README - Deploy Dental app to Azure Kubernetes Service (AKS)

This document shows steps to containerize the Spring Boot application, push the image to Azure Container Registry (ACR), create an AKS cluster, and deploy the app + MySQL to the cluster. Commands are shown for Windows (cmd.exe) and include PowerShell snippets where needed.

Prerequisites
- Azure CLI installed and authenticated: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli
- kubectl installed (az can install it: `az aks install-cli`)
- Docker installed
- An Azure subscription with appropriate permissions
- Azure Container Registry (ACR) will be created in the steps below

High-level steps
1. Build the application jar locally
2. Create Azure Container Registry (ACR) and build/push Docker image
3. Create AKS cluster
4. Create Kubernetes namespace and secrets (MySQL and JWT)
5. Apply k8s manifests
6. Access the application via the Service LoadBalancer

Detailed steps

Replace RESOURCE_GROUP with your Azure resource group name, ACR_NAME with your container registry name (must be globally unique), and LOCATION (e.g. eastus, westus2, centralus) as needed.

1) Build the application jar
From the project root in cmd.exe:

```cmd
mvnw.cmd -DskipTests package
```

This produces target/dental-0.0.1-SNAPSHOT.jar used by the Docker build.

2) Create Azure Container Registry and push Docker image

Set your Azure variables (replace with your values):

```cmd
set RESOURCE_GROUP=dental-rg
set ACR_NAME=dentalacr
set LOCATION=eastus
set CLUSTER_NAME=dental-aks-cluster
```

Login to Azure:

```cmd
az login
```

Create a resource group (if not already created):

```cmd
az group create --name %RESOURCE_GROUP% --location %LOCATION%
```

Create an Azure Container Registry:

```cmd
az acr create --resource-group %RESOURCE_GROUP% --name %ACR_NAME% --sku Basic
```

Login to ACR:

```cmd
az acr login --name %ACR_NAME%
```

Build and push the image to ACR:

```cmd
set IMAGE=%ACR_NAME%.azurecr.io/dental-app:latest
docker build -t %IMAGE% .
docker push %IMAGE%
```

Alternatively, you can use ACR to build the image directly (recommended):

```cmd
az acr build --registry %ACR_NAME% --image dental-app:latest .
```

3) Create an AKS cluster

Create the AKS cluster with 2 nodes:

```cmd
az aks create --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME% --node-count 2 --enable-addons monitoring --generate-ssh-keys --attach-acr %ACR_NAME%
```

Note: The `--attach-acr` flag allows AKS to pull images from ACR without additional authentication.

Get credentials for kubectl:

```cmd
az aks get-credentials --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME%
```

Verify connection:

```cmd
kubectl get nodes
```

4) Create namespace and secrets

Create the namespace:

```cmd
kubectl apply -f k8s/namespace.yaml
```

Create MySQL secret (example uses 'root' password configured in k8s/mysql-secret.yaml). You can apply the manifest or create it from the command line. To apply the provided manifest:

```cmd
kubectl apply -f k8s/mysql-secret.yaml
```

Or create from literals (replace values):

```cmd
kubectl create secret generic mysql-secret --namespace dental --from-literal=mysql-root-password=YOUR_PW --from-literal=mysql-database=ads_dental_db --from-literal=mysql-user=root
```

Create jwt secret: generate a secure 32-byte base64 secret (PowerShell) and create the k8s secret:

PowerShell (generate secret):

```powershell
$bytes = New-Object Byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); $b64 = [Convert]::ToBase64String($bytes); $b64
```

Then from cmd.exe create the secret (wrap the value in quotes):

```cmd
kubectl create secret generic jwt-secret --namespace dental --from-literal=jwt.secret="<PASTE_BASE64_VALUE_FROM_POWERSHELL>"
```

Note: the application reads property `jwt.secret` from the environment (via Spring Boot relaxed binding, environment variable name `JWT_SECRET` is set by k8s from the secret key). The manifest `k8s/app-deployment.yaml` already references `jwt-secret`.

5) Deploy MySQL and the app

```cmd
kubectl apply -f k8s/mysql-deployment.yaml
```

Wait for MySQL pod and PVC to be ready:

```cmd
kubectl get pods -n dental
kubectl get pvc -n dental
```

Now deploy the app (replace IMAGE_PLACEHOLDER with your image) using one of these approaches:

- Edit the `k8s/app-deployment.yaml` and replace `IMAGE_PLACEHOLDER` with your `%IMAGE%` (e.g., `dentalacr.azurecr.io/dental-app:latest`), then apply:

```cmd
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml
```

- Or apply the manifest then set the image via kubectl:

```cmd
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/app-service.yaml
kubectl -n dental set image deployment/dental-app dental-app=%IMAGE%
```

Check pods:

```cmd
kubectl get pods -n dental
kubectl get svc -n dental
```

6) Get external IP and test

The `dental-app` Service is a LoadBalancer; find its external IP (may take a few minutes to provision):

```cmd
kubectl get svc dental-app -n dental
```

Wait until the EXTERNAL-IP column shows an IP address (not <pending>).

Then test the endpoints. First login to get a JWT token:

```cmd
curl -X POST http://<EXTERNAL_IP>/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"password\"}"
```

Use the returned token to call APIs (example):

```cmd
curl -H "Authorization: Bearer <TOKEN>" http://<EXTERNAL_IP>/adsweb/api/v1/patients
```

Troubleshooting tips
- If you get database connection errors check that `mysql` service is reachable from the app pod: `kubectl exec -n dental <app-pod> -- ping mysql` and check logs with `kubectl logs -n dental <app-pod>`.
- If JWT validation fails after a redeploy, re-login to obtain a new token because the key changed or different secret used.
- If pods crash with memory/permission errors, check resource limits and container logs with `kubectl logs -n dental <pod-name>`.
- If image pull fails, ensure the AKS cluster is attached to ACR: `az aks update --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME% --attach-acr %ACR_NAME%`
- Check AKS cluster status: `az aks show --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME% --output table`

Security recommendations
- Use Azure Key Vault with the secrets store CSI driver for production secret management instead of plain k8s secrets.
- Enable Azure Policy for AKS to enforce security policies and compliance.
- Use Azure Active Directory integration for AKS authentication and RBAC.
- Secure MySQL with a strong password and network policies to restrict access.
- Use Azure Private Link for ACR to keep container images private.

Cleanup (when done testing)

Delete the AKS cluster:

```cmd
az aks delete --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME% --yes --no-wait
```

Delete the entire resource group (removes all resources):

```cmd
az group delete --name %RESOURCE_GROUP% --yes --no-wait
```

Additional Azure-specific tips
- Monitor your AKS cluster via Azure Portal > Kubernetes services > your cluster > Insights
- Use Azure Monitor and Container Insights for advanced monitoring and logging
- Scale your cluster: `az aks scale --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME% --node-count 3`
- Upgrade Kubernetes version: `az aks upgrade --resource-group %RESOURCE_GROUP% --name %CLUSTER_NAME% --kubernetes-version <version>`

That's it! Your dental application is now running on Azure Kubernetes Service with MySQL backend and can be accessed via the LoadBalancer external IP.

