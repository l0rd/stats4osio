package com.github.l0rd.stats4osio;

import static com.github.l0rd.stats4osio.OsioStatsGenerator.printTeamHTMLStats;
import static org.junit.jupiter.api.Assertions.*;

import com.mitchellbosecke.pebble.error.PebbleException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OsioStatsGeneratorTest {

  @BeforeEach
  void setUp() {
  }

  @Test
  void shouldReturnOneIssueCountWhenUsingLabelOneTest() throws IOException {

    String gitHubOrg = "l0rd";
    String gitHubRepo = "stats4osio";
    Stats4OsioGitHubClient client = new Stats4OsioGitHubClient(new String[]{});
    String team = "team/stats4osio";
    String SEV1_URGENT = "SEV1-urgent";

    OsioTeamStats teamStats = new OsioTeamStats(gitHubOrg, gitHubRepo, client, team);
    int sev1Number = teamStats.getIssuesNumber(gitHubOrg, gitHubRepo, client, team, SEV1_URGENT);
    assertEquals(1, sev1Number);
  }

  @Test
  void shouldReturnAnHTMLTableWhenInvokingPrintTeamHTMLStats() throws IOException, PebbleException {

    String gitHubOrg = "l0rd";
    String gitHubRepo = "stats4osio";
    Stats4OsioGitHubClient client = new Stats4OsioGitHubClient(new String[]{});
    String team = "team/stats4osio";

    List<String> teamsLabels = new ArrayList<>();
    teamsLabels.add(team);

    printTeamHTMLStats(gitHubOrg, gitHubRepo, client, teamsLabels);
  }
}