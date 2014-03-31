package org.funtester.plugin.fest.model;

import org.funtester.common.report.TestExecutionReport;
import org.funtester.common.report.TestExecutionReportRepository;
import org.funtester.plugin.fest.json.JsonMapper;

public class JsonTestExecutionReportRepository implements
		TestExecutionReportRepository {
	
	private final String fileName;
	
	public JsonTestExecutionReportRepository(final String fileName) {
		this.fileName = fileName;
	}

	public TestExecutionReport first() throws Exception {
		return JsonMapper.readObject( fileName, TestExecutionReport.class );
	}

	public void save(TestExecutionReport obj) throws Exception {
		JsonMapper.writeObject( fileName, obj );
	}

}
