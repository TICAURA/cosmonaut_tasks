podTemplate(containers:[
    containerTemplate(name: 'gradle', image: 'gradle:6.7.0-jdk11-hotspot', command: 'sleep', args: '99d'),
    containerTemplate(name: 'docker-registry', image: 'gcr.io/kaniko-project/executor:v1.6.0-debug', command: 'sleep', args: '99d'),
    containerTemplate(name: 'container-registry', image: 'gcr.io/google.com/cloudsdktool/cloud-sdk:327.0.0', command: 'sleep', args: '99d')
  ], volumes: [
  persistentVolumeClaim(mountPath: '/root/.m2/repository', claimName: 'maven-repo', readOnly: false),
  persistentVolumeClaim(mountPath: '/root/.gradle', claimName: 'gradle-repo', readOnly: false),
    secretVolume(secretName: 'k8s-sql-secret', mountPath: '/secret')
  ],envVars:[envVar(key:'GOOGLE_APPLICATION_CREDENTIALS',value:'/secret/cosmonaut-k8s-qa-sa_key.json')] ) {
  node(POD_LABEL) {
    stage("Compilacion") {
        container('gradle'){
            script{
			   git url: 'https://github.com/ASG-BPM/cosmonaut_tasks',branch:'main',credentialsId: 'winter_user'
			   tag = sh(script:'git describe --tags --always `git rev-list --tags` | grep PROD | head -1',returnStdout: true ).trim()
			   sh "git checkout $tag"
			   sh "gradle assemble -Ptag=$tag"
		   }
        }
     }
     stage("Push de imagen") {
        container('docker-registry'){
            script{
				sh "/kaniko/executor -f `pwd`/Dockerfile -c `pwd` --cache=true --destination=gcr.io/cosmonaut-uat/cosmonaut/tasks:$tag"
			}
        }
     }
     stage('Deploy to Kubernetes') {
        container('container-registry'){
            script{
               git url: 'https://github.com/ASG-BPM/cosmonaut_tasks',branch:'main',credentialsId: 'winter_user'
               sh "git checkout -f $tag"
		       sh 'gcloud auth activate-service-account cosmonaut-k8s-qa-sa@cosmonaut-uat.iam.gserviceaccount.com --key-file=/secret/cosmonaut-k8s-qa-sa_key.json'
               sh "gcloud container clusters get-credentials qa-backend-cosmonaut --region=us-east1-b --project=cosmonaut-uat"
		      sh 'ssh-keygen -t rsa -q -f "$HOME/.ssh/id_rsa" -N ""'
		      sh ('gcloud beta compute ssh --quiet --zone "us-east1-b" "qa-vpn-cosmonaut" --project "cosmonaut-uat" -- -N -L127.0.0.1:443:10.147.1.2:443 & > /tmp/out.txt')
		      sh 'sleep 45'
		      sh "sed -i 's/10.147.1.2/127.0.0.1/' /root/.kube/config"
		      sh "kubectl  --insecure-skip-tls-verify config view"
		      sh "kubectl  --insecure-skip-tls-verify get pods"
		       sh "sed -i 's/\$TAG/$tag/' src/jenkins/cosmonaut_tasks_deployment-qa.yml"
		       sh "cat src/jenkins/cosmonaut_tasks_deployment-qa.yml | kubectl --insecure-skip-tls-verify apply -f -"
		    
		     }
        }
     }
     }     
  }
  

