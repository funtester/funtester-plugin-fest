package org.funtester.plugin.fest.model;

import org.funtester.common.semantic.SemanticTestSuite;


/**
 * Repository of {@code SemanticTestSuite} objects.
 * 
 * @author Thiago Delgado Pinto
 *
 */
public interface SemanticTestSuiteRepository {

	/**
	 * Returns the first {@code SemanticTestSuite}.
	 * @return
	 * @throws Exception
	 */
	SemanticTestSuite first() throws Exception;
	
}
