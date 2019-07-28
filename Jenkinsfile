node {
   stage('SCM Checkout'){
    // Clone repo
	git 'https://github.com/lnimmagadda/sla-calc'
   }
   
	
   stage('Mvn Package'){
	   // Build using maven
	   
	  bat "mvn clean verify"
   }
   
}
