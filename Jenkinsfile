pipeline {
    agent any

    tools {
        // Configure this Maven installation under Manage Jenkins > Tools.
        maven 'Maven'
    }

    environment {
        AWS_REGION     = 'ap-southeast-2'
        AWS_ACCOUNT_ID = '200090082080'
        ECR_REPOSITORY = 'product-service'
        ECS_CLUSTER    = 'product-service-cluster'
        ECS_SERVICE    = 'product-service'
        REGISTRY       = '200090082080.dkr.ecr.ap-southeast-2.amazonaws.com'
        IMAGE_TAG      = "${BUILD_NUMBER}"
        DOCKER_HOST    = 'npipe:////./pipe/dockerDesktopLinuxEngine'
    }

    stages {
        stage('Test and package') {
            steps {
                bat 'mvn --batch-mode clean verify'
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn --batch-mode org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=product-service -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
                }
            }
        }
        stage('Build and push image') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins-deploy']]) {
                    bat 'aws ecr get-login-password --region %AWS_REGION% | docker login --username AWS --password-stdin %REGISTRY%'
                    bat 'docker build --tag %REGISTRY%/%ECR_REPOSITORY%:%IMAGE_TAG% .'
                    bat 'docker push %REGISTRY%/%ECR_REPOSITORY%:%IMAGE_TAG%'
                }
            }
        }

        stage('Deploy to ECS') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-jenkins-deploy']]) {
                    powershell '''
                        $ErrorActionPreference = 'Stop'
                        $image = '{0}/{1}:{2}' -f $env:REGISTRY, $env:ECR_REPOSITORY, $env:IMAGE_TAG
                        Write-Host "Deploying image: $image"
                        $taskDefinition = Get-Content 'ecs-task-definition.json' -Raw | ConvertFrom-Json
                        $taskDefinition.containerDefinitions[0].image = $image
                        $renderedPath = Join-Path $env:WORKSPACE 'ecs-task-definition-rendered.json'
                        $json = $taskDefinition | ConvertTo-Json -Depth 100
                        $utf8NoBom = [System.Text.UTF8Encoding]::new($false)
                        [System.IO.File]::WriteAllText($renderedPath, $json, $utf8NoBom)

                        $registered = aws ecs register-task-definition --cli-input-json "file://$renderedPath" --region $env:AWS_REGION | ConvertFrom-Json
                        if ($LASTEXITCODE -ne 0) { throw 'Could not register the ECS task definition.' }
                        $taskDefinitionArn = $registered.taskDefinition.taskDefinitionArn
                        if (-not $taskDefinitionArn) { throw 'AWS did not return a task definition ARN.' }

                        aws ecs update-service --cluster $env:ECS_CLUSTER --service $env:ECS_SERVICE --task-definition $taskDefinitionArn --region $env:AWS_REGION | Out-Null
                        if ($LASTEXITCODE -ne 0) { throw 'Could not update the ECS service.' }

                        aws ecs wait services-stable --cluster $env:ECS_CLUSTER --services $env:ECS_SERVICE --region $env:AWS_REGION
                        if ($LASTEXITCODE -ne 0) { throw 'ECS service did not reach a stable state.' }
                    '''
                }
            }
        }
    }

    post {
        always {
            echo 'Jenkins pipeline finished.'
        }
    }
}
