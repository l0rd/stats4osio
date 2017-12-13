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

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.egit.github.core.Label;

class OsioStatsGenerator {

  public static void main(String[] args) throws IOException, PebbleException {

    String gitHubOrg = "openshiftio";
    String gitHubRepo = "openshift.io";

    Stats4OsioGitHubClient client = new Stats4OsioGitHubClient(args);
    List<Label> teamsLabels = getTeamsLabels(gitHubOrg, gitHubRepo, client);
//    printTeamCSVStats(gitHubOrg, gitHubRepo, client, teamsLabels);

    printTeamHTMLStats(gitHubOrg, gitHubRepo, client, teamsLabels.stream().map(label -> label.getName()).collect(
        Collectors.toList()));
  }

  private static List<Label> getTeamsLabels(String gitHubOrg, String gitHubRepo,
      Stats4OsioGitHubClient client) throws IOException {
    List<Label> labels = client.getLabelService().getLabels(gitHubOrg, gitHubRepo);
    labels.removeIf(l -> !l.getName().startsWith("team/"));
    return labels;
  }

  private static void printTeamCSVStats(String gitHubOrg, String gitHubRepo,
      Stats4OsioGitHubClient client, List<Label> teamLabels) throws IOException {
    printCSVHeader();

    for (Label teamLabel : teamLabels) {
      OsioTeamStats teamStats = new OsioTeamStats(gitHubOrg,
                                                  gitHubRepo,
                                                  client,
                                                  teamLabel.getName());

      String team = teamStats.getTeamName();
      int sev1Number = teamStats.getSev1IssuesNumber();
      String sev1URL = teamStats.getSev1IssuesURL();
      int sev2Number = teamStats.getSev2IssuesNumber();
      String sev2URL = teamStats.getSev2IssuesURL();

      System.out.println(
          team + " ," + sev1Number + " ," + sev1URL + " ," + sev2Number + " ," + sev2URL);
    }
  }

  private static void printCSVHeader() {
    System.out.println("team, SEV1-COUNT, SEV1-URL, SEV2-COUNT, SEV2-URL");
  }

  public static void printTeamHTMLStats(String gitHubOrg, String gitHubRepo,
      Stats4OsioGitHubClient client, List<String> teamLabels) throws PebbleException, IOException {

    PebbleEngine engine = new PebbleEngine.Builder().build();
    PebbleTemplate compiledTemplate = engine.getTemplate("templates/issues_table.html");

    Writer writer = new StringWriter();
    Map<String, Object> context = new HashMap<>();
    context.put("websiteTitle", "OpenShift.io GitHub stats");

    List<OsioTeamStats> osioTeamsStats = new ArrayList<>();
    for (String teamLabel : teamLabels) {
      OsioTeamStats teamStats = new OsioTeamStats(gitHubOrg,
          gitHubRepo,
          client,
          teamLabel);
      osioTeamsStats.add(teamStats);
    }

    context.put("osioTeams", osioTeamsStats);
    context.put("now", Calendar.getInstance().getTime());

    compiledTemplate.evaluate(writer, context);
    String output = writer.toString();
    System.out.println(output);
  }
}
