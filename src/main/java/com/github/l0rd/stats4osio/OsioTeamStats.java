package com.github.l0rd.stats4osio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.service.IssueService;

public class OsioTeamStats {

  private static final String SEV1_URGENT = "SEV1-urgent";
  private static final String SEV2_HIGH = "SEV2-high";

  private String teamName;
  private int sev1IssuesNumber;
  private int sev2IssuesNumber;
  private String sev1IssuesURL;
  private String sev2IssuesURL;

  public OsioTeamStats(String gitHubOrg, String gitHubRepo, Stats4OsioGitHubClient client, String teamLabel)
      throws IOException {
    teamName = teamLabel;
    sev1IssuesNumber = getIssuesNumber(gitHubOrg, gitHubRepo, client, teamName, SEV1_URGENT);
    sev1IssuesURL = getFilteredIssuesURL(gitHubOrg, gitHubRepo, teamName, SEV1_URGENT);
    sev2IssuesNumber = getIssuesNumber(gitHubOrg, gitHubRepo, client, teamName, SEV2_HIGH);
    sev2IssuesURL = getFilteredIssuesURL(gitHubOrg, gitHubRepo, teamName, SEV2_HIGH);
  }

  public int getSev1IssuesNumber() {
    return sev1IssuesNumber;
  }
  public int getSev2IssuesNumber() {
    return sev2IssuesNumber;
  }
  public String getSev1IssuesURL() {
    return sev1IssuesURL;
  }
  public String getSev2IssuesURL() {
    return sev2IssuesURL;
  }
  public String getTeamName() { return teamName; }

  public int getIssuesNumber(String gitHubOrg, String gitHubRepo,
      Stats4OsioGitHubClient client, String teamLabel, String sevLabel) throws IOException {

    IssueService svc = client.getIssueService();

    HashMap<String, String> blockerFilter = new HashMap<String, String>() {{
      put(IssueService.FILTER_LABELS, sevLabel + "," + teamLabel);
    }};

    List<Issue> issues = svc.getIssues(gitHubOrg, gitHubRepo, blockerFilter);
    return issues.size();
  }

  public String getFilteredIssuesURL(String gitHubOrg, String gitHubRepo, String team,
      String sev) {
    try {
      return new URIBuilder("https://github.com/" + gitHubOrg + "/" + gitHubRepo + "/issues")
          .addParameter("q", "is:open is:issue label:" + team + " label:" + sev).build().toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return "";
  }

}
