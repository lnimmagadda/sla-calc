pipeline {
	agent any 
 	stage('SCM Checkout'){
 	 git 'https://github.com/lnimmagadda/sla-calc'
 	}
 	stage('Docker build') {
		docker.build('jenkins-test')
	}
	
  
}
