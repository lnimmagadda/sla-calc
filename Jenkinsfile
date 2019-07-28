node {
 	stage('SCM Checkout'){
 	 git 'https://github.com/lnimmagadda/sla-calc'
 	}
 
	stage('Docker build') {
		docker.build('jenkins-test')
	}
	
	stage('Docker push'){
		docker.withRegistry('https://633377509572.dkr.ecr.us-east-2.amazonaws.com', 'ecr:us-east-2:jenkins-test') {
    			docker.image('jenkins-test').push('latest')
  		}
	}
  
}
