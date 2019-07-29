node {
 	stage('SCM Checkout'){
 	 git 'https://github.com/lnimmagadda/sla-calc'
 	}
 	stage('Docker build') {
		docker.build('jenkins-test')
	}
	stage('Docker push'){
		sh("eval \$(aws ecr get-login --no-include-email --region us-east-2| sed 's|https://||')")
		docker.withRegistry('https://633377509572.dkr.ecr.us-east-2.amazonaws.com', 'f373cf05-8724-4a06-83c2-169e75ec2695') {
		 docker.image('jenkins-test').push('latest')  
	 	}
	}
	 
  
}
