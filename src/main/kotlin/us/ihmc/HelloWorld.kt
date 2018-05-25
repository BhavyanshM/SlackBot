package us.ihmc

import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

fun main(args: Array<String>) {
	val session = SlackSessionFactory.createWebSocketSlackSession("AUTH_TOKEN_FOR_SLACK_BOT_USER");
	session.connect();
	session.addMessagePostedListener({ posted, session ->
		if (posted.sender.userName == "Bhavyansh Mishra")
			session.sendMessage(posted.channel, "This is infinite automated Slack bot.", null);
	});
}


