# Quick Start Guide - REST Assured CI/CD Pipeline

## Option 1: Automated Setup (Using CloudFormation) ⚡

### Prerequisites:
- AWS Account with appropriate permissions
- GitHub repository with this code
- GitHub Personal Access Token (with repo permissions)

### Steps:

1. **Get GitHub Personal Access Token**:
   - Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Generate new token with `repo` scope
   - Copy the token (you'll use it in step 3)

2. **Upload Code to GitHub**:
   ```bash
   cd rest-assured-cicd
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```

3. **Deploy CloudFormation Stack**:
   - Go to AWS CloudFormation Console
   - Click "Create stack" → "With new resources"
   - Upload `cloudformation-template.yml`
   - Fill in parameters:
     - **GitHubRepo**: `your-username/your-repo-name`
     - **GitHubBranch**: `main`
     - **GitHubToken**: Paste your GitHub token
   - Click "Next" → "Next" → Check "I acknowledge..." → "Create stack"
   - Wait 5-10 minutes for completion

4. **Verify Setup**:
   - Go to CodePipeline Console
   - You should see `rest-assured-pipeline` running
   - Check CodeBuild Console for test results

✅ **Done!** Your pipeline is now active and will run on every push.

---

## Option 2: Manual Setup (Step-by-Step) 🔧

### Step 1: Push Code to Git Repository

```bash
cd rest-assured-cicd
git init
git add .
git commit -m "Initial commit"
git remote add origin <YOUR_GIT_REPO_URL>
git push -u origin main
```

### Step 2: Create S3 Bucket

```bash
# Using AWS CLI
aws s3 mb s3://rest-assured-artifacts-$(aws sts get-caller-identity --query Account --output text)

# Or use AWS Console:
# S3 → Create bucket → Name it → Create
```

### Step 3: Create IAM Role for CodeBuild

**Using AWS Console:**
1. IAM → Roles → Create role
2. Select "AWS service" → "CodeBuild"
3. Attach policies:
   - Create custom policy with S3 and CloudWatch access (see template)
4. Name: `RestAssuredCodeBuildRole`
5. Create role

### Step 4: Create CodeBuild Project

**Using AWS Console:**
1. CodeBuild → Create build project
2. **Project configuration:**
   - Name: `rest-assured-build`
3. **Source:**
   - Provider: GitHub (or your choice)
   - Repository: Select your repo
   - Branch: `main`
4. **Environment:**
   - Managed image
   - OS: Amazon Linux 2
   - Runtime: Standard
   - Image: `aws/codebuild/amazonlinux2-x86_64-standard:4.0`
   - Service role: `RestAssuredCodeBuildRole`
5. **Buildspec:**
   - Use buildspec file: `buildspec.yml`
6. **Artifacts:**
   - Type: S3
   - Bucket: Your S3 bucket
   - Path: `builds/`
7. **Logs:** Enable CloudWatch logs
8. Create build project

### Step 5: Create CodePipeline

**Using AWS Console:**
1. CodePipeline → Create pipeline
2. **Pipeline settings:**
   - Name: `rest-assured-pipeline`
   - New service role
3. **Source stage:**
   - Provider: GitHub (or your choice)
   - Repository and branch: Select yours
   - Detection: CloudWatch Events (recommended)
4. **Build stage:**
   - Provider: AWS CodeBuild
   - Project: `rest-assured-build`
5. **Skip deploy stage**
6. Create pipeline

### Step 6: Test the Pipeline

```bash
# Make a small change
echo "# Test commit" >> README.md
git add .
git commit -m "Test pipeline trigger"
git push
```

Watch the pipeline execute in AWS Console!

---

## Option 3: Test Locally First 🖥️

Before setting up AWS, verify tests work locally:

```bash
# Prerequisites: Install Java 11+ and Maven

# Navigate to project
cd rest-assured-cicd

# Run tests
mvn clean test

# Check results
ls -la target/surefire-reports/
```

Expected output:
```
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

If tests pass locally, proceed with AWS setup using Option 1 or 2.

---

## Viewing Test Results

### After Pipeline Runs:

1. **CodeBuild Console**:
   - CodeBuild → Build history → Select your build
   - Click "Reports" tab
   - View detailed test results

2. **S3 Bucket**:
   - S3 → Your artifact bucket → `builds/`
   - Download test reports

3. **CloudWatch Logs**:
   - CloudWatch → Log groups → `/aws/codebuild/rest-assured-build`
   - View detailed build logs

---

## Common Issues & Solutions

### ❌ Build fails with "Unable to locate credentials"
**Solution**: Check IAM role permissions on CodeBuild project

### ❌ Tests fail with connection timeout
**Solution**: Ensure CodeBuild has internet access (not in private VPC without NAT)

### ❌ Pipeline doesn't trigger on push
**Solution**: 
- Check webhook configuration in GitHub
- Verify branch name matches
- Check CodePipeline source settings

### ❌ "Access Denied" to S3
**Solution**: Update IAM role policy to include S3 bucket permissions

---

## What Happens When You Push Code?

1. 🔔 **Git Push** → GitHub receives your commit
2. 🚀 **Webhook Trigger** → CodePipeline detects change
3. 📦 **Source Stage** → Downloads code from GitHub
4. 🏗️ **Build Stage** → CodeBuild starts:
   - Installs Java & Maven
   - Downloads dependencies (cached after first run)
   - Compiles code
   - Runs REST Assured tests
   - Generates test reports
5. 📊 **Reports** → Test results available in AWS Console
6. 💾 **Artifacts** → Uploaded to S3 bucket
7. ✅ **Complete** → Pipeline shows success/failure

---

## Next Steps

- [ ] Set up SNS notifications for pipeline failures
- [ ] Add more test cases
- [ ] Integrate with Slack/Email for alerts
- [ ] Add code coverage reports
- [ ] Create deploy stage if needed
- [ ] Add manual approval gate
- [ ] Set up scheduled test runs

---

## Need Help?

- Check the detailed README.md
- Review CloudWatch logs for errors
- Check AWS CodeBuild documentation
- Verify all IAM permissions

Happy Testing! 🎉
