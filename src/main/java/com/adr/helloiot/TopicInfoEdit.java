//    HelloIoT is a dashboard creator for MQTT
//    Copyright (C) 2017-2018 Adrián Romero Corchado.
//
//    This file is part of HelloIot.
//
//    HelloIot is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    HelloIot is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with HelloIot.  If not, see <http://www.gnu.org/licenses/>.
//
package com.adr.helloiot;

import com.adr.fonticon.FontAwesome;
import com.adr.fonticon.IconBuilder;
import com.adr.helloiot.device.DeviceBasic;
import com.adr.helloiot.device.DeviceSimple;
import com.adr.helloiot.device.TransmitterSimple;
import com.adr.helloiot.device.format.StringFormat;
import com.adr.helloiot.device.format.StringFormatBase64;
import com.adr.helloiot.device.format.StringFormatDecimal;
import com.adr.helloiot.device.format.StringFormatHex;
import com.adr.helloiot.device.format.StringFormatIdentity;
import com.adr.helloiot.unit.EditAreaEvent;
import com.adr.helloiot.unit.EditAreaStatus;
import com.adr.helloiot.unit.EditAreaView;
import com.adr.helloiot.unit.EditEvent;
import com.adr.helloiot.unit.EditStatus;
import com.adr.helloiot.unit.EditView;
import com.adr.helloiot.unit.Unit;
import com.adr.helloiot.unit.UnitPage;
import com.adr.helloiot.util.ExternalFonts;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

/**
 *
 * @author adrian
 */
public class TopicInfoEdit implements TopicInfo {

    private final static String STYLEFORMAT = "{} {-fx-background-color: gray; -fx-background-radius: 10px; -fx-fill:white; -fx-padding: 0 5 0 5; -fx-pref-width: 70px; -fx-text-alignment: center;}";
    private final static String STYLEFORMATSPACE = "{} {-fx-padding: 0 5 0 5; -fx-pref-width: 70px;}";
    private final static String STYLEQOS = "{} {-fx-background-color: darkblue; -fx-background-radius: 10px; -fx-fill:white; -fx-padding: 0 5 0 5; -fx-pref-width: 30px; -fx-text-alignment: center;}";
    private final static String STYLEQOSSPACE = "{} {-fx-padding: 0 5 0 5; -fx-pref-width: 30px;}";

    private final String type;  
    private final TopicInfoEditNode editnode;

    private String page = null;
    private String topic = null;
    private final SimpleStringProperty name = new SimpleStringProperty();
    private String topicpub = null;
    private String format = "STRING";
    private String jsonpath = null;
    private boolean multiline = false;
    private Color color = null;
    private Color background = null;
    private int qos = -1;
    private int retained = -1;

    public TopicInfoEdit(String type, TopicInfoEditNode editnode) {
        this.type = type;
        this.editnode = editnode;
    }
    
    @Override
    public String getType() {
        return type;
    }

    @Override
    public ReadOnlyProperty<String> getLabel() {
        return name;
    }
    
    @Override
    public Node getGraphic() {
        
        Text t;
        if ("Subscription".equals(getType())) {
            t = IconBuilder.create(FontAwesome.FA_COMMENT, 12.0).build();
        } else if ("Publication".equals(getType())) {
            t = IconBuilder.create(FontAwesome.FA_SEND, 12.0).build();
        } else { // "Publication/Subscription"
            t = IconBuilder.create(FontAwesome.FA_PENCIL, 12.0).build();
        }        
        t.setFill(Color.WHITE);
        TextFlow tf = new TextFlow(t);
        tf.setTextAlignment(TextAlignment.CENTER);
        tf.setPadding(new Insets(5, 8, 5, 8));
        tf.setStyle("-fx-background-color: #4559d4; -fx-background-radius: 5px;");
        tf.setPrefWidth(30.0);
        return tf;         
    }
    
    @Override
    public void load(SubProperties properties) {
        name.setValue(properties.getProperty(".name"));
        page = properties.getProperty(".page", null);
        topic = properties.getProperty(".topic", null);
        topicpub = properties.getProperty(".topicpub", null);
        format = properties.getProperty(".format", "STRING");
        jsonpath = properties.getProperty(".jsonpath", null);
        multiline = Boolean.parseBoolean(properties.getProperty(".multiline", "false"));
        String c = properties.getProperty(".color", null);
        color = c == null ? null : Color.valueOf(c);
        c = properties.getProperty(".background", null);
        background = c == null ? null : Color.valueOf(c);    
        qos = Integer.parseInt(properties.getProperty(".qos", "-1"));
        retained = Integer.parseInt(properties.getProperty(".retained", "-1"));
   }
    
    @Override
    public void store(SubProperties properties) {
        properties.setProperty(".type", getType());

        properties.setProperty(".name", name.getValue());
        properties.setProperty(".page", page);
        properties.setProperty(".topic", topic);
        properties.setProperty(".topicpub", topicpub);
        properties.setProperty(".format", format);
        properties.setProperty(".jsonpath", jsonpath);
        properties.setProperty(".multiline", Boolean.toString(multiline));
        properties.setProperty(".color", color == null ? null : color.toString());
        properties.setProperty(".background", background == null ? null : background.toString());
        properties.setProperty(".qos", Integer.toString(qos));
        properties.setProperty(".retained", Integer.toString(retained));        
    }
    
    @Override
    public TopicStatus getTopicStatus() throws HelloIoTException {
        
        if (topic == null || topic.isEmpty()) {
            ResourceBundle resources = ResourceBundle.getBundle("com/adr/helloiot/fxml/main");
            String label = getLabel().getValue();
            throw new HelloIoTException(resources.getString("exception.topicinfoedit"));
        }
        
        if ("Subscription".equals(getType())) {
            return buildTopicSubscription();
        } else if ("Publication".equals(getType())) {
            return buildTopicPublish();
        } else { // "Publication/Subscription"
            return buildTopicPublishSubscription();
        }
    }

    @Override
    public TopicInfoNode getEditNode() {
        return editnode;
    }

    @Override
    public void writeToEditNode() {
        editnode.editname.setText(name.getValue());
        editnode.editpage.setValue(page);
        editnode.edittopic.setText(topic);
        editnode.edittopicpub.setText(topicpub);
        editnode.edittopicpub.setDisable("Subscription".equals(getType()));
        editnode.editformat.getSelectionModel().select(format);
        editnode.editjsonpath.setText(jsonpath);
        editnode.editjsonpath.setDisable("BASE64".equals(format) || "HEX".equals(format) || "SWITCH".equals(format));
        editnode.editmultiline.setSelected(multiline);
        editnode.editcolor.setValue(color);
        editnode.editbackground.setValue(background);
        editnode.editqos.setValue(qos);
        editnode.editretained.setValue(retained);     
    }

    @Override
    public void readFromEditNode() {
        name.setValue(editnode.editname.getText());
        topic = editnode.edittopic.getText();
        if ("Subscription".equals(type)) {
            topicpub = null;
            editnode.edittopicpub.setDisable(true);
        } else {
            editnode.edittopicpub.setDisable(false);
            topicpub = editnode.edittopicpub.getText() == null || editnode.edittopicpub.getText().isEmpty() ? null : editnode.edittopicpub.getText();
        }
        format = editnode.editformat.getValue();
        if ("BASE64".equals(format) || "HEX".equals(format) || "SWITCH".equals(format)) {
            jsonpath = null;
            editnode.editjsonpath.setDisable(true);
        } else {
            jsonpath = editnode.editjsonpath.getText();
            editnode.editjsonpath.setDisable(false);
        }
        multiline = editnode.editmultiline.isSelected();
        color = editnode.editcolor.getValue();
        background = editnode.editbackground.getValue();
        qos = editnode.editqos.getValue();
        retained = editnode.editretained.getValue();  
    }  
    
    private TopicStatus buildTopicPublish() {

        TransmitterSimple d = new TransmitterSimple();
        d.setTopic(topic);
        d.setTopicPublish(topicpub);
        d.setQos(qos);
        if (retained >= 0) {
            d.setRetained(retained != 0);
        }
        d.setFormat(createFormat());

        EditEvent u = multiline ? new EditAreaEvent() : new EditEvent();
        u.setPrefWidth(320.0);
        u.setLabel(getLabel().getValue());
        u.setFooter(topic + getQOSBadge(qos) + getFormatBadge(d.getFormat()));
        setStyle(u);
        u.setDevice(d);
        UnitPage.setPage(u, page);

        return new TopicStatus(Arrays.asList(d), Arrays.asList(u));
    }

    private TopicStatus buildTopicPublishSubscription() {

        DeviceSimple d = new DeviceSimple();
        d.setTopic(topic);
        d.setTopicPublish(topicpub);
        d.setQos(qos);
        if (retained >= 0) {
            d.setRetained(retained != 0);
        }
        d.setFormat(createFormat());

        EditStatus u = multiline ? new EditAreaStatus() : new EditStatus();
        u.setPrefWidth(320.0);
        u.setLabel(getLabel().getValue());
        u.setFooter(topic + getQOSBadge(qos) + getFormatBadge(d.getFormat()));
        setStyle(u);
        u.setDevice(d);
        UnitPage.setPage(u, page);
        
        return new TopicStatus(Arrays.asList(d), Arrays.asList(u));
    }

    private TopicStatus buildTopicSubscription() {

        DeviceBasic d = new DeviceBasic();
        d.setTopic(topic);
        d.setTopicPublish(topicpub);
        d.setQos(qos);
        if (retained >= 0) {
            d.setRetained(retained != 0);
        }
        d.setFormat(createFormat());

        EditView u = multiline ? new EditAreaView() : new EditView();
        u.setPrefWidth(320.0);
        u.setLabel(getLabel().getValue());
        u.setFooter(topic + getQOSBadge(qos) + getFormatBadge(d.getFormat()));
        setStyle(u);
        u.setDevice(d);
        UnitPage.setPage(u, page);

        return new TopicStatus(Arrays.asList(d), Arrays.asList(u));
    }
    
    private void setStyle(Unit u) {
        StringBuilder style = new StringBuilder();
        if (color != null) {
            style.append("-fx-level-fill: ").append(webColor(color)).append(";");
        }
        if (background != null) {
            style.append("-fx-background-unit: ").append(webColor(background)).append(";");
        }
        u.getNode().setStyle(style.toString());        
    }

    private String getFormatBadge(StringFormat f) {
        if (f instanceof StringFormatIdentity) {
            return STYLEFORMATSPACE;
        } else {
            return STYLEFORMAT + f.toString();
        }
    }

    private String getQOSBadge(int i) {
        if (i < 0) {
            return STYLEQOSSPACE;
        } else {
            return STYLEQOS + Integer.toString(i);
        }
    }

    private StringFormat createFormat() {
        if ("STRING".equals(format)) {
            return new StringFormatIdentity(jsonpath == null || jsonpath.isEmpty() ? null : jsonpath);
        } else if ("INT".equals(format)) {
            return new StringFormatDecimal(jsonpath == null || jsonpath.isEmpty() ? null : jsonpath, "0");
        } else if ("BASE64".equals(format)) {
            return new StringFormatBase64();
        } else if ("HEX".equals(format)) {
            return new StringFormatHex();
        } else if ("DOUBLE".equals(format)) {
            return new StringFormatDecimal(jsonpath == null || jsonpath.isEmpty() ? null : jsonpath, "0.00");
        } else if ("DECIMAL".equals(format)) {
            return new StringFormatDecimal(jsonpath == null || jsonpath.isEmpty() ? null : jsonpath, "0.000");
        } else if ("DEGREES".equals(format)) {
            return new StringFormatDecimal(jsonpath == null || jsonpath.isEmpty() ? null : jsonpath, "0.0°");
        } else {
            return StringFormatIdentity.INSTANCE;
        }
    }    
    
    private String webColor(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255));
    }    
}
