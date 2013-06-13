abstract class HudsonBaseModel  {

	String displayName = "Shaw Systems Jenkins Server Farm"

	static DynamicConfigurationInterface globalConfig
	
	List serverList // Constructed server objects with received XML
	List ipAddressList
	Map pipelineSpecs
	Map pipelineModel
	boolean isLiveQueryEnabled // if true, gather XML Api results.   If false, assume unreachable and merely display URL on page
	
	HudsonBaseModel() {
		HudsonBaseModel( true )
	}
	
	HudsonBaseModel( boolean isLiveQueryEnabled ) {
		this.isLiveQueryEnabled = isLiveQueryEnabled
		serverList = new ArrayList()
		populateModel()
		this.pipelineSpecs = globalConfig.getPipelineSpecs()
		this.pipelineModel = createPipelineModel()
	}

	abstract void populateModel()

	// Object model of pipeline job status
	def createPipelineModel() {
		def model = [:]
		this.pipelineSpecs.each { key, value ->
			List jobUrl = value
			def jobStats = []
			jobUrl.each {
				String urlString = it.jobUrl + "/lastBuild/api/xml"
				def xml
				try { 
					xml = this.isLiveQueryEnabled ? new URL( urlString ).text : ""
				} catch (Exception e) {
					xml = null
				}
				jobStats << new JenkinsJobStatus( it.displayName, urlString, xml )
			}
			model.put ( key, jobStats )
		}
		return model
	}
	
	void parseIpString ( String ipString ) {
		this.ipAddressList = ipString.tokenize(",")
	}

	int size() { 
		return serverList.size() 
	}
	
	void add( HudsonServer hs ) { 
		serverList.add( hs ) 
	}
	
	static DynamicConfigurationInterface getDynamicConfiguration() {
		if ( globalConfig == null ) { 
			println ''
			println 'Loading configuration from /DynamicConfiguration.groovy...'
			println ''
			def inputStream = DynamicConfigurationInterface.class.getResourceAsStream("/DynamicConfiguration.groovy")
			String groovySource = inputStream.getText()
			GroovyClassLoader gcl = new GroovyClassLoader();
			Class clazz = gcl.parseClass(groovySource);
			globalConfig = clazz.newInstance();
		}
		return globalConfig
	}
	 
}

