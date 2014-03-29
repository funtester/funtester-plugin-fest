package org.funtester.plugin.fest.model;

import org.funtester.common.semantic.SemanticTestSuite;
import org.funtester.plugin.fest.json.JsonMapper;

/**
 * Repository of one {@code SemanticTestSuite} object in a JSON file.
 * 
 * @author Thiago Delgado Pinto
 *
 */
public class JsonSemanticTestSuiteRepository implements SemanticTestSuiteRepository {
	
	private final String filePath;

	public JsonSemanticTestSuiteRepository(final String jsonFilePath) {
		this.filePath = jsonFilePath;
	}

	public SemanticTestSuite first() throws Exception {
		return JsonMapper.readObject( filePath, SemanticTestSuite.class );
	}

}
