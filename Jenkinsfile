@Library("Utilities") _
BuildAndDeploy {
    sshKeyAttributeName = "key"
    confFile = [dev : "dev-fq.conf", sandbox : "sbx-fq.conf", prod : "prd-us-fq.conf"]
    securityGroups = [dev : "fuelquest_dev_allow_ssh,fuelquest_dev_tomcat" ,
                      sandbox : "fuelquest_sbx_allow_ssh,fuelquest_sbx_tomcat",
                      prod: "fuelquest_prd_allow_ssh,fuelquest_prd_tomcat"]
    albTargetGroup = [dev : "fuelquest-dev-slacalc" ,
                      sandbox : "fuelquest-sbx-slacalc",
                      prod: "fuelquest-prd-slacalc"]
}