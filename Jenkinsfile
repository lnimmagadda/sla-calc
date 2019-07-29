node {
 	stage('SCM Checkout'){
 	 git 'https://github.com/lnimmagadda/sla-calc'
 	}
 	stage('Docker build') {
		docker.build('jenkins-test')
	}
	stage('Docker push'){
		sh("eval \$(aws ecr get-login --no-include-email --region us-east-2 | sed 's|https://||')")
		docker.withRegistry('https://633377509572.dkr.ecr.us-east-2.amazonaws.com/jenkins-test') {
		 docker.image('jenkins-test').push('latest')  
	 	}
	}
	 
  
}
