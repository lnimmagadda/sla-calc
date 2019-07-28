node {
   def mvn = tool (name: 'Maven3', type: 'maven') + '/bin/mvn'
   stage('SCM Checkout'){
    // Clone repo
	git branch: 'master', 
	credentialsId: 'github', 
	url: 'https://github.com/lnimmagadda/sla-calc'
   
   }
   
	
   stage('Mvn Package'){
	   // Build using maven
	   
	   sh "${mvn} clean package "
   }
   
}
