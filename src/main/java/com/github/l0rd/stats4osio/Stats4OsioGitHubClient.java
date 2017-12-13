package com.github.l0rd.stats4osio;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Stats4OsioGitHubClient {

  final Logger logger = LoggerFactory.getLogger(OsioStatsGenerator.class);
  private final IssueService issueService;
  private final LabelService labelService;

  public Stats4OsioGitHubClient(String[] args) {
    issueService = new IssueService();
    labelService = new LabelService();
    initServicesAuthentication(args);
  }

  private void initServicesAuthentication(String[] args) {
    GitHubClient client = new GitHubClient();
    if (args.length == 1) {
      String token = args[0];
      client.setOAuth2Token(token);
      if (issueService != null) {issueService.getClient().setOAuth2Token(token);}
      if (labelService != null) {labelService.getClient().setOAuth2Token(token);}
    } else if (args.length == 2) {
      String user = args[0];
      String password = args[1];
      client.setCredentials(user, password);
      if (issueService != null) {issueService.getClient().setCredentials(user, password);}
      if (labelService != null) {labelService.getClient().setCredentials(user, password);}
    } else {
      logger.warn(
          "We are going to use GitHub API without authentication and the rate limite is very low. "
              + "Authenticated request instead get a much higher limit (c.f. https://developer.github.com/v3/#rate-limiting). "
              + "To use authenticated requests provide a GitHub access token as a parameter when running OsioStatsGenerator. "
              + "For how to get a GitHub access token see https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/");
    }
  }

  public LabelService getLabelService() {
    return labelService;
  }
  public IssueService getIssueService() { return issueService; }
}
