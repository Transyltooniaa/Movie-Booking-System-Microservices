pipeline {
    agent any

    triggers {
        // REQUIRED so GitHub webhook triggers this job
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                // This binds the repo to the webhook — REQUIRED for trigger to work
                checkout scm
            }
        }

        stage("Detect Changed Service") {
            steps {
                script {
                    echo "Detecting changed folders..."

                    // Get changed files between last commit and current
                    def changedFiles = sh(
                        script: "git diff --name-only HEAD~1 HEAD || true",
                        returnStdout: true
                    ).trim().split("\n")

                    echo "Changed files: ${changedFiles}"

                    // Map folder -> Jenkins job name
                    def serviceMap = [
                        "user-service": "User-Service-Pipeline",
                        "movie-service": "Movie-Service-Pipeline",
                        "booking-service": "Booking-Service-Pipeline",
                        "payment-service": "Payment-Service-Pipeline",
                        "notification-service": "Notification-Service-Pipeline",
                        "api-gateway": "api-gateway-pipeline"
                    ]

                    env.TARGET_JOB = ""

                    // Check which service folder changed
                    changedFiles.each { file ->
                        serviceMap.each { folder, jobName ->
                            if (file.startsWith(folder + "/")) {
                                env.TARGET_JOB = jobName
                            }
                        }
                    }

                    if (!env.TARGET_JOB) {
                        echo "No microservice changes detected. Skipping."
                        currentBuild.result = 'SUCCESS'
                        // Stop pipeline here
                        error("NO_CHANGES")
                    }

                    echo "Detected service change → triggering job: ${env.TARGET_JOB}"
                }
            }
        }

        stage("Trigger Microservice Pipeline") {
            steps {
                script {
                    build job: env.TARGET_JOB, wait: true
                }
            }
        }
    }

    post {
        success {
            echo "Orchestrator complete."
        }
    }
}
