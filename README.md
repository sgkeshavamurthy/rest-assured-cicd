# REST Assured CI/CD Pipeline with AWS CodePipeline

This project demonstrates a simple REST Assured test automation framework integrated with AWS CI/CD pipeline using CodePipeline and CodeBuild.

## Project Structure

```
rest-assured-cicd/
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── tests/
│                       └── SimpleGetRequestTest.java
├── pom.xml
├── testng.xml
├── buildspec.yml
├── .gitignore
└── README.md
```

## Prerequisites

- AWS Account
- Git repository (GitHub, GitLab, AWS CodeCommit, or Bitbucket)
- Basic knowledge of AWS services
- Maven installed locally (for local testing)
- Java 11 or higher

## Test Details

The project includes REST Assured tests that perform GET requests against the JSONPlaceholder API:

- **Test 1**: Verify status code 200
- **Test 2**: Verify content type
- **Test 3**: Verify response body fields
- **Test 4**: Extract and validate response data
- **Test 5**: Verify GET all posts
- **Test 6**: Verify GET with query parameters

## Running Tests Locally

```bash
# Clone the repository
git clone <your-repo-url>
cd rest-assured-cicd

# Run tests
mvn clean test

# View test reports
open target/surefire-reports/index.html
```

## AWS CI/CD Pipeline Setup

### Step 1: Create S3 Bucket for Artifacts

1. Go to AWS S3 Console
2. Click **Create bucket**
3. Name: `rest-assured-cicd-artifacts-<your-account-id>`
4. Region: Choose your preferred region
5. Keep default settings and click **Create bucket**

### Step 2: Create IAM Role for CodeBuild

1. Go to IAM Console → **Roles** → **Create role**
2. Select **AWS service** → **CodeBuild** → **Next**
3. Attach these policies:
   - `AmazonS3FullAccess` (or create a custom policy with specific bucket access)
   - `CloudWatchLogsFullAccess`
4. Name: `CodeBuildServiceRole`
5. Click **Create role**

### Step 3: Create CodeBuild Project

1. Go to AWS CodeBuild Console
2. Click **Create build project**
3. Configure:
   - **Project name**: `rest-assured-build`
   - **Source provider**: Choose your Git provider (GitHub, CodeCommit, etc.)
   - **Repository**: Connect and select your repository
   - **Branch**: `main` or `master`
   
4. **Environment**:
   - Environment image: `Managed image`
   - Operating system: `Amazon Linux 2`
   - Runtime: `Standard`
   - Image: `aws/codebuild/amazonlinux2-x86_64-standard:4.0`
   - Service role: Select `CodeBuildServiceRole` created earlier
   
5. **Buildspec**:
   - Build specifications: `Use a buildspec file`
   - Buildspec name: `buildspec.yml`
   
6. **Artifacts**:
   - Type: `Amazon S3`
   - Bucket name: Select the bucket you created
   - Name: `rest-assured-artifacts`
   - Path: `builds/`
   - Artifacts packaging: `Zip`
   
7. **Logs**:
   - CloudWatch logs: Enable
   - Group name: `/aws/codebuild/rest-assured-build`
   - Stream name: `build-log`

8. Click **Create build project**

### Step 4: Create CodePipeline

1. Go to AWS CodePipeline Console
2. Click **Create pipeline**
3. **Pipeline settings**:
   - Name: `rest-assured-pipeline`
   - Service role: Create new service role
   - Artifact store: Default location or custom S3 bucket
   
4. **Add source stage**:
   - Source provider: Choose your Git provider
   - Repository: Select your repository
   - Branch: `main` or `master`
   - Detection options: `AWS CodePipeline` (webhook)
   
5. **Add build stage**:
   - Build provider: `AWS CodeBuild`
   - Project name: Select `rest-assured-build`
   
6. **Skip deploy stage** (click Skip)

7. Review and click **Create pipeline**

### Step 5: Configure Test Reporting (Optional but Recommended)

1. In CodeBuild project settings
2. Go to **Reports**
3. The buildspec.yml already includes report configuration
4. Test reports will be automatically generated and viewable in:
   - CodeBuild Console → Reports
   - Each build's test results tab

### Step 6: Set Up Notifications (Optional)

1. Go to AWS SNS Console
2. Create a topic: `rest-assured-pipeline-notifications`
3. Create subscription (Email/SMS)
4. In CodePipeline, go to your pipeline
5. Settings → Notifications → Create notification rule
6. Configure events: Failed, Succeeded
7. Select SNS topic created above

## Pipeline Flow

1. **Developer pushes code** to Git repository
2. **CodePipeline** automatically detects the change (via webhook)
3. **CodeBuild** pulls the code and:
   - Installs Java and Maven
   - Resolves dependencies
   - Compiles the code
   - Runs REST Assured tests
   - Generates test reports
   - Uploads artifacts to S3
4. **Test reports** are available in CodeBuild Console
5. **Notifications** sent (if configured)

## Viewing Test Results

### In AWS Console:

1. Go to CodeBuild Console
2. Click on your build project
3. Select a build run
4. Go to **Reports** tab to see test results
5. Click on report group to see detailed test execution

### Test Report Includes:

- Total tests run
- Passed/Failed count
- Test duration
- Individual test case results
- Stack traces for failures

## Buildspec.yml Explained

```yaml
phases:
  install: # Install Java runtime
  pre_build: # Resolve Maven dependencies
  build: # Compile the code
  post_build: # Run tests

reports: # Configure test reporting
  TestReport:
    files: '**/*'
    base-directory: 'target/surefire-reports'
    file-format: 'JUNITXML'

artifacts: # Files to store in S3
  files:
    - target/**/*.jar
    - target/surefire-reports/**/*

cache: # Cache Maven dependencies for faster builds
  paths:
    - '/root/.m2/**/*'
```

## Troubleshooting

### Build Fails with "No tests were executed"

- Ensure `testng.xml` is in the project root
- Check that test class names match in `testng.xml`
- Verify Maven Surefire plugin configuration in `pom.xml`

### Permission Denied Errors

- Check IAM role permissions for CodeBuild
- Ensure S3 bucket policy allows CodeBuild to write

### Tests Pass Locally but Fail in Pipeline

- Check CodeBuild environment settings (Java version)
- Review CloudWatch logs for detailed error messages
- Verify network connectivity if tests need external APIs

### Pipeline Not Triggering Automatically

- Check webhook configuration in source settings
- Verify branch name matches
- Check CodePipeline service role permissions

## Cost Considerations

- CodeBuild: Pay per build minute (free tier: 100 build minutes/month)
- CodePipeline: $1 per active pipeline per month (free tier: 1 pipeline)
- S3: Storage costs for artifacts
- CloudWatch Logs: Log storage costs

## Best Practices

1. **Use Maven Dependency Caching** - Already configured in buildspec.yml
2. **Keep Artifacts** - Store test reports and JARs for debugging
3. **Monitor Build Times** - Optimize slow builds
4. **Set Up Notifications** - Get alerted on failures
5. **Use IAM Best Practices** - Principle of least privilege
6. **Tag Resources** - For cost allocation and organization
7. **Review Test Reports** - Regularly check for flaky tests

## Extending the Pipeline

### Add More Test Classes:

1. Create new test class in `src/test/java/com/example/tests/`
2. Add class to `testng.xml`
3. Push to repository

### Add Deploy Stage:

After tests pass, you can add a deploy stage to:
- Deploy to ECS/EKS
- Trigger Lambda functions
- Deploy to Elastic Beanstalk

### Add Manual Approval:

Add a manual approval stage between build and deploy:
1. Edit pipeline
2. Add stage
3. Choose "Manual approval"

## Resources

- [REST Assured Documentation](https://rest-assured.io/)
- [AWS CodeBuild Documentation](https://docs.aws.amazon.com/codebuild/)
- [AWS CodePipeline Documentation](https://docs.aws.amazon.com/codepipeline/)
- [Maven Documentation](https://maven.apache.org/)

## Support

For issues or questions:
- Check CloudWatch Logs for detailed error messages
- Review CodeBuild build logs
- Verify IAM permissions
- Check AWS service quotas

## License

This project is provided as-is for educational purposes.
