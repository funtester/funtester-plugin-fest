package org.funtester.plugin.fest.model;

import org.funtester.common.generation.TestGenerationConfiguration;
import org.funtester.common.generation.TestGenerationConfigurationRepository;
import org.funtester.plugin.fest.json.JsonMapper;

/**
 * Repository of one {@code TestGenerationConfiguration} object in a JSON file.
 * 
 * @author Thiago Delgado Pinto
 *
 */
public class JsonTestGenerationConfigurationRepository implements TestGenerationConfigurationRepository {

	private final String filePath;
	
	public JsonTestGenerationConfigurationRepository(final String jsonFilePath) {
		this.filePath = jsonFilePath;
	}
	
	public TestGenerationConfiguration first() throws Exception {
		return JsonMapper.readObject( filePath, TestGenerationConfiguration.class );
	}

}
