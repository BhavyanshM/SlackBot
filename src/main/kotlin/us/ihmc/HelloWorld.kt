package us.ihmc

import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.rtm.RTMClient
import com.ullink.slack.simpleslackapi.SlackAttachment
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.omg.PortableInterceptor.USER_EXCEPTION
import org.json.JSONObject as JSONObject
import javax.swing.text.html.parser.Parser



fun main(args: Array<String>) {

    // CREDS GO HERE


    val CROSS_MARK_EMOJI = ":bamboo_failed:"
    val CHECK_MARK_EMOJI = ":bamboo_passed:"


    val session = SlackSessionFactory.createWebSocketSlackSession(SLACK_ACCESS_TOKEN);
	session.connect();
	session.addMessagePostedListener({ posted, session ->
//		if (posted.sender.userName == "bhavyansh")
//		{}
        println(posted.sender.id)
		if(posted.messageContent.contains("+release status")){
            val client = OkHttpClient();

            var planNumber = -1
            val packageName = posted.messageContent.removePrefix("+release status ").replace(" ", "")

            val requestBamboo = Request.Builder()
                    .url("https://bamboo.ihmc.us/rest/api/latest/result.json?os_authType=basic&max-result=50")
                    .header("Authorization", Credentials.basic(BAMBOO_USERNAMe, BAMBOO_PASSWORD))
                    .build()
            val requestBintray = Request.Builder()
                    .url("https://api.bintray.com/packages/ihmcrobotics/maven-release/${packageName}")
                    .header("Authorization", Credentials.basic(BINTRAY_USERNAME, BINTRAY_API_KEY))
                    .build()
            val requestGithub = Request.Builder()
                    .url("https://api.github.com/repos/ihmcrobotics/${packageName}/issues")
                    .build()
            val requestArtifactory = Request.Builder()
                    .url("http://10.6.6.221:8081/artifactory/api/search/latestVersion?g=us.ihmc&a=exoskeleton")
                    .header("Authorization", Credentials.basic(BINTRAY_USERNAME, ARTIFACTORY_API_KEY))

            val responseBamboo = client.newCall(requestBamboo).execute()
            val responseBintray = client.newCall(requestBintray).execute()
            val responseGithub = client.newCall(requestGithub).execute()

            val dataBintray = responseBintray.body()!!.string()
            val dataBamboo = responseBamboo.body()!!.string()
            val dataGithub = responseGithub.body()!!.string()

//            if(dataBintray.contains("not found"))
//                val responseArtifactory = client.newCall(requestArtifactory).execute()



            val jsonBamboo = JSONObject(dataBamboo)
            val jsonBintray = JSONObject(dataBintray)
            val jsonGithub = JSONArray(dataGithub)

            val plansJSONArray = jsonBamboo.getJSONObject("results").getJSONArray("result")
            val commandString = posted.messageContent
            for(i in 0..(plansJSONArray.length() - 1)){
                val obj = plansJSONArray.getJSONObject(i)
                val planName = obj
                        .getJSONObject("plan")
                        .getString("shortName")
                        .toLowerCase()
                        .replace(" ", "-")
                val inputPlanName = commandString.removePrefix("+release status ")
                if(inputPlanName.contains(planName)){
                    planNumber = i
                    break
                }
            }

            val bambooPlanObject = plansJSONArray.getJSONObject(planNumber)
            val bambooPlanShortName = bambooPlanObject
                    .getJSONObject("plan")
                    .getString("shortName")
            val buildStatus = bambooPlanObject
                    .getString("state").toString()
            var buildStatusOutput = "${bambooPlanShortName}\n" +
                    "Build Status: ${buildStatus} "
            if(buildStatus.contains("Failed"))
                buildStatusOutput += CROSS_MARK_EMOJI + "\n"
            else
                buildStatusOutput += CHECK_MARK_EMOJI + "\n"


            val bintrayPackageVersion = jsonBintray.getString("latest_version")
            val bintrayPackageDateUpdated = jsonBintray.getString("updated")
            val bintrayOutput = "Version Number: ${bintrayPackageVersion}\nLast Release: ${bintrayPackageDateUpdated}\n"


            val totalNumberOfIssues = jsonGithub.length()
            val githubOutput = "Outstanding Issues: ${totalNumberOfIssues}\n"


            val output = "${buildStatusOutput}${bintrayOutput}${githubOutput}"

            val slackment = SlackAttachment()
            slackment.setFallback("Project Statistics")
            slackment.setColor("#7CD197")
            slackment.setPretext("Project Statistics for ${bambooPlanShortName}")
            slackment.setText(output)
//            {
//                "attachments": [
//                {
//                    "fallback": "New ticket from Andrea Lee - Ticket #1943: Can't reset my password - https://groove.hq/path/to/ticket/1943",
//                    "pretext": "New ticket from Andrea Lee",
//                    "title": "Ticket #1943: Can't reset my password",
//                    "title_link": "https://groove.hq/path/to/ticket/1943",
//                    "text": "Help! I tried to reset my password but nothing happened!",
//                    "color": "#7CD197"
//                }
//                ]
//            }
            session.sendMessage(posted.channel, null, slackment)

		}
	});



    }


