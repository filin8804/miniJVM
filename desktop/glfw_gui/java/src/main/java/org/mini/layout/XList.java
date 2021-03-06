package org.mini.layout;

import org.mini.gui.GImage;
import org.mini.gui.GList;
import org.mini.gui.GListItem;
import org.mini.gui.GObject;
import org.mini.gui.event.GActionListener;
import org.mini.gui.event.GStateChangeListener;
import org.mini.layout.gscript.Interpreter;
import org.mini.layout.gscript.Str;
import org.mini.layout.xmlpull.KXmlParser;
import org.mini.layout.xmlpull.XmlPullParser;

import java.util.Vector;

public class XList extends XObject implements GStateChangeListener {
    static public final String XML_NAME = "list";

    protected static class ListItem implements GActionListener {
        static public final String XML_NAME = "li";

        XList xlist;
        String name;
        String text;
        String pic;
        String cmd;
        String onClick;

        @Override
        public void action(GObject gobj) {
            if (onClick != null) {
                Interpreter inp = xlist.getRoot().getInp();
                // 执行脚本
                if (inp != null) {
                    inp.putGlobalVar("cmd", new Str(cmd));
                    inp.callSub(onClick);
                }
            }
            xlist.getRoot().getEventHandler().action(gobj, cmd);
        }
    }

    protected Vector items = new Vector();
    protected boolean multiLine = false;
    protected boolean multiSelect = false;
    protected int itemheight = XDef.DEFAULT_LIST_HEIGHT;

    protected GList list;

    public XList() {
        super(null);
    }

    public XList(XContainer xc) {
        super(xc);
    }

    @Override
    protected String getXmlTag() {
        return XML_NAME;
    }

    protected void parseMoreAttribute(String attName, String attValue) {
        super.parseMoreAttribute(attName, attValue);
        if (attName.equals("multiline")) {
            multiLine = "0".equals(attValue) ? false : true;
        } else  if(attName.equals("multiselect")) {
            multiSelect = "0".equals(attValue) ? false : true;
        } else if (attName.equals("itemh")) {
            itemheight = Integer.parseInt(attValue);
        }
    }


    /**
     * 解析
     *
     * @param parser KXmlParser
     * @throws Exception
     */
    public void parse(KXmlParser parser) throws Exception {
        super.parse(parser);
        int depth = parser.getDepth();

        //得到域
        do {
            parser.next();
            String tagName = parser.getName();
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                if (tagName.equals(XList.ListItem.XML_NAME)) {
                    XList.ListItem item = new ListItem();
                    item.xlist = this;
                    item.name = parser.getAttributeValue(null, "name");
                    item.pic = parser.getAttributeValue(null, "pic");
                    item.cmd = parser.getAttributeValue(null, "cmd");
                    item.onClick = parser.getAttributeValue(null, "onclick");
                    String tmp = parser.nextText();
                    item.text = tmp == null ? "" : tmp;
                    items.add(item);
                }
                toEndTag(parser, XList.ListItem.XML_NAME);
                parser.require(XmlPullParser.END_TAG, null, tagName);
            }
        }
        while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(getXmlTag()) && depth == parser.getDepth()));

    }


    protected int getDefaultWidth(int parentViewW) {
        return parentViewW;
    }

    protected int getDefaultHeight(int parentViewH) {
        if (multiLine) {
            return XDef.DEFAULT_LIST_HEIGHT * items.size();
        } else {
            return XDef.DEFAULT_LIST_HEIGHT;
        }
    }


    public GObject getGui() {
        return list;
    }

    protected void createGui() {
        if (list == null) {
            list = new GList(x, y, width, height);
            list.setShowMode(multiLine ? GList.MODE_MULTI_SHOW : GList.MODE_SINGLE_SHOW);
            list.setName(name);
            list.setItemHeight(itemheight);
            list.setXmlAgent(this);
            list.setStateChangeListener(this);
            for (int i = 0; i < items.size(); i++) {
                ListItem item = (ListItem) items.elementAt(i);
                GImage img = null;
                if (item.pic != null) {
                    img = GImage.createImageFromJar(item.pic);
                }
                GListItem gli = new GListItem(img, item.text);
                gli.setName(item.name);
                gli.setActionListener(item);

                list.add(gli);
            }
        } else {
            list.setLocation(x, y);
            list.setSize(width, height);
        }
    }


    @Override
    public void onStateChange(GObject gobj) {
        getRoot().getEventHandler().onStateChange(gobj, null);
    }
}
