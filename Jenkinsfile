
pipeline {
  agent any
  parameters {
    string(name: 'REPONAME', defaultValue: 'sla-calculator', description: 'AWS ECR Repository Name')
    string(name: 'ECR', defaultValue: '633377509572.dkr.ecr.us-east-2.amazonaws.com/sla-calculator', description: 'AWS ECR Registry URI')
    string(name: 'REGION', defaultValue: 'us-east-2', description: 'AWS Region code')
    string(name: 'CLUSTER', defaultValue: 'sla-calculator-cluster', description: 'AWS ECS Cluster name')
    string(name: 'TASK', defaultValue: 'sla-calculator', description: 'AWS ECS Task name')
  }
  stages {
    stage('SCM Checkout'){
 	   git 'https://github.com/lnimmagadda/sla-calc'
 	  }
    stage('DeployStage') {
      steps {
        sh "./deploy.sh -b ${env.BUILD_ID} -e ${params.ECR} -c ${params.CLUSTER} -t ${params.TASK}"
      }
    }

  }
}
