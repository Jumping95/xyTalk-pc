package xysoft.im.iq;

import java.io.IOException;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import xysoft.im.app.Launcher;

public class Gateway extends IQ {

    private String jid;
    private String username;
    public static final String ELEMENT_NAME = "query";
    public static final String NAMESPACE = "jabber:iq:gateway";

    protected Gateway()
    {
        super( ELEMENT_NAME, NAMESPACE );
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder( IQChildElementXmlStringBuilder buf )
    {
        buf.rightAngleBracket();
        buf.append("<query xmlns=\"").append(NAMESPACE).append("\">");
        buf.append("<prompt>").append(username).append("</prompt>");
        buf.append("</query>");
        return buf;
    }

    public static class Provider extends IQProvider<Gateway> {

        public Provider() {
            super();
        }

		@Override
		public Gateway parse(XmlPullParser parser, int initialDepth)
				throws XmlPullParserException, IOException, SmackException {
			Gateway version = new Gateway();

            boolean done = false;
            while (!done) {
                int eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("jid")) {
                        version.setJid(parser.nextText());
                    }
                    else if (parser.getName().equals("username")) {
                        version.setUsername(parser.nextText());
                    }
                }

                else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals(ELEMENT_NAME)) {
                        done = true;
                    }
                }
            }

            return version;
		}
    }

    /**
     * Returns the fully qualified JID of a user.
     *
     * @param serviceName the service the user belongs to.
     * @param username    the name of the user.
     * @return the JID.
     */
    public static String getJID(String serviceName, String username) throws SmackException.NotConnectedException
    {
        Gateway registration = new Gateway();
        registration.setType(IQ.Type.set);
        registration.setTo(serviceName);
        registration.setUsername(username);

        XMPPConnection con = Launcher.connection;
        PacketCollector collector = con.createPacketCollector(new StanzaIdFilter(registration.getStanzaId()));
        try
        {
            con.sendStanza( registration );

            Gateway response = collector.nextResult( SmackConfiguration.getDefaultPacketReplyTimeout() );
            return response.getJid();
        }
        finally
        {
            collector.cancel();
        }
    }


}