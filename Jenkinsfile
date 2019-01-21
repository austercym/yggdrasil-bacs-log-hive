@Library('jenkins-shared-libs@feature/lib_refactor')_

pipeline {


    agent { label 'docker'}


    environment {
        ARTIFACTORY_SERVER_REF = 'artifactory'

        artifactVersion = "${new Date().format('yy.MM.dd')}"
        pomPath = 'pom.xml'

        snapshotRepository = 'libs-snapshot-local'
        releaseRepository = 'libs-release-local'
        snapshotDependenciesRepository = 'libs-snapshot'
        releaseDependeciesRepository = 'libs-release'
    }

	parameters {
        string(name: 'HAS_CHANGES', defaultValue: 'N')
        string(name: 'IS_MASTER', defaultValue: 'N')
    }
    
    stages {

        stage('Pipeline setup') {
            parallel {
                stage('Triggers setup') {
                    agent{
                        docker {
                            image 'maven:3-alpine'
                            args '-v $HOME/.m2:/root/.m2 --network user-default'
                            reuseNode true
                            label 'docker'
                        }
                    }
                    steps {
                        script {
                            triggerStarter  ((env.JOB_NAME.tokenize('/'))[0])
                           
                        }
                    }
                }

                stage('Artifactory setup') {
                    agent{
                        docker {
                            image 'maven:3-alpine'
                            args '-v $HOME/.m2:/root/.m2 --network  user-default'
                            reuseNode true
                            label 'docker'
                        }
                    }
                    steps {
                        script {
                            def MAVEN_HOME = sh(script: 'echo $MAVEN_HOME', returnStdout: true).trim()
                            // Obtain an Artifactory server instance, defined in Jenkins --> Manage:
                            server = Artifactory.server ARTIFACTORY_SERVER_REF

                            def  descriptor = Artifactory.mavenDescriptor()
                          //  def end = '-SNAPSHOT'
                            descriptor.pomFile = pomPath
                            def scmVars = checkout scm
                            if (!( scmVars.GIT_BRANCH == 'master'))  {
                                artifactVersion = artifactVersion + '-SNAPSHOT'
                            }
                            if ( scmVars.GIT_BRANCH == 'master')  {
                                env.IS_MASTER='Y'
                            }
                            if (scmVars.GIT_COMMIT != scmVars.GIT_PREVIOUS_COMMIT) {
                                env.HAS_CHANGES='Y'
                            }                            
                            descriptor.version = artifactVersion
                            descriptor.transform()

                            rtMaven = Artifactory.newMavenBuild()
                            env.MAVEN_HOME = MAVEN_HOME
                            rtMaven.deployer releaseRepo: releaseRepository, snapshotRepo: snapshotRepository, server: server
                            rtMaven.resolver releaseRepo: releaseDependeciesRepository, snapshotRepo: snapshotDependenciesRepository, server: server
                            rtMaven.opts = '-DprofileIdEnabled=true'
                            rtMaven.deployer.deployArtifacts = false // Disable artifacts deployment during Maven run

                            buildInfo = Artifactory.newBuildInfo()
                        }
                    }
                }
            }
        }

        stage('Unit test') {
            when {
        		expression { env.HAS_CHANGES == 'Y' }
        		beforeAgent true
      		}
            agent{
                docker {
                    image 'maven:3-alpine'
                    args '-v $HOME/.m2:/root/.m2 --network user-default'
                    reuseNode true
                    label 'docker'
                }
            }
            steps {

                withEnv(["https_proxy=squid.service.cicd.consul:3128"]) {
                    script {
                        rtMaven.run pom: pomPath, goals: '-U clean test -Pdeploy', buildInfo: buildInfo
                    }
                }
            }

           /* post {
                  always {
                      junit 'target/surefire-reports/*.xml'
                  }
            }
	    */
        }

	stage('SonarAnalysis') {
		agent {
			docker {
				image 'docker.registry.service.cicd.consul/sonarqube-scanner:latest'
				args '-u root:root'
				reuseNode true
			}
		}
		steps {
			withSonarQubeEnv('SonarQube') {
				sh "sudo sonar-scanner -Dsonar.host.url=http://11.0.200.26:9000 -Dsonar.projectKey=${env.JOB_NAME.replace('/','_')} -Dsonar.sources=./src/main -Dsonar.java.binaries=."
			}
		}
		post {
                    always {
                        echo 'inside post always sonar stage'
			sh 'rm -rf .scannerwork'
                    }
                }
	}

        stage('Build') {
            when {
        		expression { env.HAS_CHANGES == 'Y' }
        		beforeAgent true
      		}
            agent{
                docker {
                    image 'maven:3-alpine'
                    args '-v $HOME/.m2:/root/.m2 --network user-default'
                    reuseNode true
                    label 'docker'
                }
            }
            steps {
                script {
                    rtMaven.run pom: pomPath, goals: '-U clean package -DskipTests -Pdeploy', buildInfo: buildInfo
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: '**/target/*.jar'
                }
            }
        }

        stage('Publish') {
            when {
        		expression { env.HAS_CHANGES == 'Y' }
        		beforeAgent true
      		}
            agent{
                docker {
                    image 'maven:3-alpine'
                    args '-v $HOME/.m2:/root/.m2 --network user-default'
                    reuseNode true
                    label 'docker'
                }
            }

            steps {
                script {
                    server.publishBuildInfo buildInfo
                    rtMaven.deployer.deployArtifacts buildInfo
                }

            }
        }

       stage('Deploy') {
            when {
        		expression { env.HAS_CHANGES == 'Y' && env.IS_MASTER == 'N' }
        		beforeAgent true
      		}
            steps {
                script {
                    pom = readMavenPom file: pomPath
                    pom = readMavenPom file: pomPath
                    kUser =  'svc_bacs'
                    hostsDeploy = 'sid-hdf-g4-1.node.sid.consul'
                    nimbusHost = 'sid-hdf-g1-1.node.sid.consul'
                    zkHost = 'sid-hdf-g1-0.node.sid.consul:2181,sid-hdf-g1-1.node.sid.consul:2181,sid-hdf-g1-2.node.sid.consul:2181'
                    mainClass = 'com.orwellg.yggdrasil.bacs.log.hive.topology.BacsLogSaveToHive'
                    groupId = 'com.orwellg.yggdrasil'
                    stormDeploy  pom.artifactId  , pom.version , groupId, kUser  ,  hostsDeploy , nimbusHost ,zkHost, mainClass
                }
            }


        }
        
    }
}
