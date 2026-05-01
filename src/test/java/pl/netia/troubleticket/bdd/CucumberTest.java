package pl.netia.troubleticket.bdd;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = "cucumber.glue", value = "pl.netia.troubleticket.bdd")
public class CucumberTest {}
