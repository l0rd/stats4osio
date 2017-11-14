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
import java.util.HashMap;
import java.util.List;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OsioStatsGenerator {

  private static final String SEVERITY_BLOCKER = "severity/blocker";
  private static final String SEVERITY_P1 = "severity/P1";

  public static void main(String[] args) throws IOException {

    final Logger logger = LoggerFactory.getLogger(OsioStatsGenerator.class);

    String gitHubOrg = "openshiftio";
    String gitHubRepo = "openshift.io";

    IssueService issueService = new IssueService();
    LabelService labelService = new LabelService();
    GitHubClient client = new GitHubClient();
    if (args.length > 0) {
      String token = args[0];
      client.setOAuth2Token(token);
      issueService.getClient().setOAuth2Token(token);
      labelService.getClient().setOAuth2Token(token);
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
      List<Issue> blockers = getBlockers(gitHubOrg, gitHubRepo, issueService, team);
      List<Issue> p1 = getP1(gitHubOrg, gitHubRepo, issueService, team);
      System.out.println(team + "," + blockers.size() + "," + p1.size());
    }
  }

  private static void printCSVHeader() {
    System.out.println("team, blockers, P1");
  }

  private static List<Issue> getBlockers(String gitHubOrg, String gitHubRepo,
      IssueService issueService, String team) throws IOException {
    HashMap<String, String> blockerFilter = new HashMap<String, String>() {{
      put(IssueService.FILTER_LABELS, SEVERITY_BLOCKER + "," + team);
    }};
    return issueService.getIssues(gitHubOrg, gitHubRepo, blockerFilter);
  }

  private static List<Issue> getP1(String gitHubOrg, String gitHubRepo,
      IssueService issueService, String team) throws IOException {
    HashMap<String, String> blockerFilter = new HashMap<String, String>() {{
      put(IssueService.FILTER_LABELS, SEVERITY_P1 + "," + team);
    }};
    return issueService.getIssues(gitHubOrg, gitHubRepo, blockerFilter);
  }
}
