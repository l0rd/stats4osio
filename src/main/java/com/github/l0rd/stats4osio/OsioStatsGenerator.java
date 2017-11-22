/**
 * The MIT License (MIT)
 * Copyright (c) 2017 Mario Loriedo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.l0rd.stats4osio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OsioStatsGenerator {

  private static final String SEV1_URGENT = "SEV1-urgent";
  private static final String SEV2_HIGH = "SEV2-high";

  public static void main(String[] args) throws IOException {

    final Logger logger = LoggerFactory.getLogger(OsioStatsGenerator.class);

    String gitHubOrg = "openshiftio";
    String gitHubRepo = "openshift.io";

    IssueService issueService = new IssueService();
    LabelService labelService = new LabelService();
    GitHubClient client = new GitHubClient();
    if (args.length == 1) {
      String token = args[0];
      client.setOAuth2Token(token);
      issueService.getClient().setOAuth2Token(token);
      labelService.getClient().setOAuth2Token(token);
    } else if (args.length == 2) {
      String user = args[0];
      String password = args[1];
      client.setCredentials(user, password);
      issueService.getClient().setCredentials(user, password);
      labelService.getClient().setCredentials(user, password);
    } else {
      logger.warn(
          "We are going to use GitHub API without authentication and the rate limite is very low. "
              + "Authenticated request instead get a much higher limit (c.f. https://developer.github.com/v3/#rate-limiting). "
              + "To use authenticated requests provide a GitHub access token as a parameter when running OsioStatsGenerator. "
              + "For how to get a GitHub access token see https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/");
    }

    List<Label> labels = labelService.getLabels(gitHubOrg, gitHubRepo);
    labels.removeIf(l -> !l.getName().startsWith("team/"));

    printCSVHeader();

    for (Label teamLabel : labels) {
      String team = teamLabel.getName();
      List<Issue> sev1 = getSev1(gitHubOrg, gitHubRepo, issueService, team);
      String sev1URL = getFilteredIssuesURL(gitHubOrg, gitHubRepo, team, SEV1_URGENT);
      List<Issue> sev2 = getSev2(gitHubOrg, gitHubRepo, issueService, team);
      String sev2URL = getFilteredIssuesURL(gitHubOrg, gitHubRepo, team, SEV2_HIGH);
      System.out.println(
          team + " ," + sev1.size() + " ," + sev1URL + " ," + sev2.size() + " ," + sev2URL);
    }
  }

  private static void printCSVHeader() {
    System.out.println("team, SEV1-COUNT, SEV1-URL, SEV2-COUNT, SEV2-URL");
  }

  private static List<Issue> getSev1(String gitHubOrg, String gitHubRepo,
      IssueService issueService, String team) throws IOException {
    HashMap<String, String> blockerFilter = new HashMap<String, String>() {{
      put(IssueService.FILTER_LABELS, SEV1_URGENT + "," + team);
    }};
    return issueService.getIssues(gitHubOrg, gitHubRepo, blockerFilter);
  }

  private static String getFilteredIssuesURL(String gitHubOrg, String gitHubRepo, String team,
      String sev) {
    try {
      return new URIBuilder("https://github.com/" + gitHubOrg + "/" + gitHubRepo + "/issues")
          .addParameter("q", "is:open is:issue label:" + team + " label:" + sev).build().toString();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return "";
  }

  private static List<Issue> getSev2(String gitHubOrg, String gitHubRepo,
      IssueService issueService, String team) throws IOException {
    HashMap<String, String> blockerFilter = new HashMap<String, String>() {{
      put(IssueService.FILTER_LABELS, SEV2_HIGH + "," + team);
    }};
    return issueService.getIssues(gitHubOrg, gitHubRepo, blockerFilter);
  }
}
