package us.ihmc

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.omg.PortableInterceptor.USER_EXCEPTION
import org.json.JSONObject as JSONObject
import javax.swing.text.html.parser.Parser



fun main(args: Array<String>) {

    val PLAN_NUMBER = 23
//  GOES HERE

	val session = SlackSessionFactory.createWebSocketSlackSession(SLACK_ACCESS_TOKEN);
	session.connect();
	session.addMessagePostedListener({ posted, session ->
//		if (posted.sender.userName == "bhavyansh")
//		{}
		if(posted.messageContent.contains("+release status")){
            val client = OkHttpClient();
            val request = Request.Builder()
                    .url("https://bamboo.ihmc.us/rest/api/latest/result.json?os_authType=basic")
                    .header("Authorization", Credentials.basic(BAMBOO_USERNAMe, BAMBOO_PASSWORD))
                    .build()
            val response = client.newCall(request).execute()
            println(response)
            val data = response.body()!!.string()
            val json = JSONObject(data)
            val planShortName = json.getJSONObject("results")
                    .getJSONArray("result")
                    .getJSONObject(PLAN_NUMBER)
                    .getJSONObject("plan")
                    .getString("shortName")
            val result = json.getJSONObject("results")
                    .getJSONArray("result")
                    .getJSONObject(PLAN_NUMBER)
                    .getString("state").toString()
            val output = "${planShortName}\nBuild Status: ${result}"
			session.sendMessage(posted.channel, output, null);
		}
	});



}


