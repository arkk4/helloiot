//    HelloIoT is a dashboard creator for MQTT
//    Copyright (C) 2018-2019 Adrián Romero Corchado.
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
package com.adr.helloiot.unit;

import com.adr.fonticon.IconFontGlyph;
import com.adr.fonticon.IconBuilder;
import com.adr.hellocommon.dialog.MessageUtils;
import com.adr.helloiot.HelloPlatform;
import com.adr.helloiot.device.TreePublish;
import com.adr.helloiot.mqtt.MQTTProperty;
import com.adr.helloiotlib.app.IoTApp;
import com.adr.helloiotlib.app.TopicManager;
import com.adr.helloiotlib.format.MiniVar;
import com.adr.helloiotlib.format.StringFormat;
import com.adr.helloiotlib.format.StringFormatBase64;
import com.adr.helloiotlib.format.StringFormatDecimal;
import com.adr.helloiotlib.format.StringFormatHex;
import com.adr.helloiotlib.format.StringFormatIdentity;
import com.adr.helloiotlib.format.StringFormatJSONPretty;
import com.adr.helloiotlib.unit.Unit;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 *
 * @author adrian
 */
public class PublicationsPage extends VBox implements Unit {
    
    protected TopicManager manager;
    private final ResourceBundle resources; 

    private TreePublish device = null;
    
    private ToolBar toolbar;
    private ToolBar topiccontainer;
    private Label title;
    private String label = null;
    private ComboBox<String> topic;
    private TextField delay;
    private Button sendmessage;
    private TextArea payload;
    
    private CheckBox retained;

    private ToggleGroup qosgroup;
    private RadioButton qos0;
    private RadioButton qos1;
    private RadioButton qos2;
    
    
    private ToggleGroup formatsgroup;
    private RadioButton formatplain;
    private RadioButton formatjson;
    private RadioButton formathex;
    private RadioButton formatbase64;

    public PublicationsPage() {
        resources = ResourceBundle.getBundle("com/adr/helloiot/fxml/publications");
        load();
    }

    private void load() {
        
        HBox.setHgrow(this, Priority.ALWAYS);
        
        title = new Label();
        title.getStyleClass().add("messagestitle");
        
        topic = new ComboBox<String>();
        topic.setPromptText(resources.getString("input.topic"));
        topic.setEditable(true);
        topic.setPrefWidth(200.0);
        topic.setMaxWidth(Double.MAX_VALUE);
        topic.getStyleClass().add("unitcombobox");
        HBox.setHgrow(topic, Priority.ALWAYS);
        
        sendmessage = new Button(resources.getString("button.send"));
        sendmessage.setMnemonicParsing(false);
        sendmessage.getStyleClass().add("unitbutton");
        sendmessage.setGraphic(IconBuilder.create(IconFontGlyph.FA_SOLID_PAPER_PLANE, 18.0).styleClass("icon-fill").build());
        sendmessage.setOnAction(this::actionSendMessage);
        
        topiccontainer = new ToolBar(topic, sendmessage);    
        topiccontainer.getStyleClass().add("unittoolbar");

        delay = new TextField();
        delay.setPromptText(resources.getString("input.delay"));
        delay.setEditable(true);
        delay.setPrefWidth(100.0);
        delay.setMinWidth(50.0);
        delay.getStyleClass().add("unitinput");   
        
        Separator formatsepa = new Separator();
        formatsepa.setOrientation(Orientation.VERTICAL);
        formatsepa.setFocusTraversable(false);          
        
        retained = new CheckBox(resources.getString("label.retained"));
        retained.setMnemonicParsing(false);
        retained.getStyleClass().add("unitcheckbox");  
        
        Separator formatsep0 = new Separator();
        formatsep0.setOrientation(Orientation.VERTICAL);
        formatsep0.setFocusTraversable(false);          

        qosgroup = new ToggleGroup();
        
        qos0 = new RadioButton(resources.getString("label.qos0"));
        qos0.setUserData(0);
        qos0.setMnemonicParsing(false);
        qos0.setToggleGroup(qosgroup);
        qos0.getStyleClass().add("unitradiobutton");
        qos0.setSelected(true);
        
        qos1 = new RadioButton(resources.getString("label.qos1"));
        qos1.setUserData(1);
        qos1.setMnemonicParsing(false);
        qos1.setToggleGroup(qosgroup);
        qos1.getStyleClass().add("unitradiobutton");
        
        qos2 = new RadioButton(resources.getString("label.qos2"));
        qos2.setUserData(2);
        qos2.setMnemonicParsing(false);
        qos2.setToggleGroup(qosgroup);
        qos2.getStyleClass().add("unitradiobutton");  
        
        Separator formatsep = new Separator();
        formatsep.setOrientation(Orientation.VERTICAL);
        formatsep.setFocusTraversable(false);        
        
        formatsgroup = new ToggleGroup();
        
        formatplain = new RadioButton(resources.getString("label.plain"));
        formatplain.setMnemonicParsing(false);
        formatplain.setToggleGroup(formatsgroup);
        formatplain.getStyleClass().add("unitradiobutton");
        formatplain.setUserData(StringFormatIdentity.INSTANCE);
        formatplain.setSelected(true);
        
        formatjson = new RadioButton(resources.getString("label.json"));
        formatjson.setMnemonicParsing(false);
        formatjson.setToggleGroup(formatsgroup);
        formatjson.getStyleClass().add("unitradiobutton");
        formatjson.setUserData(StringFormatJSONPretty.INSTANCE);
        
        formathex = new RadioButton(resources.getString("label.hex"));
        formathex.setMnemonicParsing(false);
        formathex.setToggleGroup(formatsgroup);
        formathex.getStyleClass().add("unitradiobutton");
        formathex.setUserData(StringFormatHex.INSTANCE);        
        
        formatbase64 = new RadioButton(resources.getString("label.base64"));
        formatbase64.setMnemonicParsing(false);
        formatbase64.setToggleGroup(formatsgroup);
        formatbase64.getStyleClass().add("unitradiobutton");
        formatbase64.setUserData(StringFormatBase64.INSTANCE);        
        
        toolbar = new ToolBar(delay, formatsepa, retained, formatsep0, qos0, qos1, qos2, formatsep, formatplain, formatjson, formathex, formatbase64);
        toolbar.getStyleClass().add("unittoolbar");
        
        payload = new TextArea();
        payload.setPromptText(resources.getString("input.message"));
        payload.setEditable(true);
        payload.getStyleClass().addAll("unitinputarea", "unitinputcode");
        VBox.setVgrow(payload, Priority.ALWAYS);
        BorderPane.setAlignment(payload, Pos.CENTER);   
        
        getChildren().addAll(topiccontainer, toolbar, payload);
    }
    
    @Override
    public void construct(IoTApp app) {
    }
    
    @Override
    public void destroy() {
    }

    @Override
    public Node getNode() {
        return this;
    }
    
    public void setLabel(String label) {
        if (getLabel() != null && !getLabel().isEmpty()) {
            topiccontainer.getItems().remove(title);
        }
        
        this.label = label;
        title.setText(label + "/");
        
        if (label != null && !label.isEmpty()) {
            topiccontainer.getItems().add(0, title);
        }
    }

    public String getLabel() {
        return label;
    }    
    
    public void setDevice(TreePublish device) {
        this.device = device;
        
        if (getLabel() == null || getLabel().isEmpty()) {
            String proplabel = device.getProperties().getProperty("label");
            setLabel(proplabel == null || proplabel.isEmpty() ? device.getTopic() : proplabel);
        }                 
    }
    
    public TreePublish getDevice() {
        return device;
    }
    
    private void actionSendMessage(ActionEvent ev) {
        MiniVar delayvalue;
        try {
            delayvalue = StringFormatDecimal.INTEGER.parse(delay.getText());
        } catch (IllegalArgumentException ex) {
            MessageUtils.showException(MessageUtils.getRoot(this), resources.getString("label.sendmessage"), resources.getString("message.delayerror"), ex);
            return;
        }
        
        if (delayvalue.asInt() < 0) {
            MessageUtils.showError(MessageUtils.getRoot(this), resources.getString("label.sendmessage"), resources.getString("message.delayerror"));
            return;                
        }        
        
        try {
            MQTTProperty.setQos(device, (int) qosgroup.getSelectedToggle().getUserData());
            MQTTProperty.setRetained(device, retained.isSelected());

            StringFormat format = (StringFormat) formatsgroup.getSelectedToggle().getUserData();
            device.setFormat(format);
            if (delayvalue.asInt() > 0) {
                device.sendMessage(topic.getEditor().getText(), format.parse(payload.getText()), delayvalue.asInt());
            } else {
                device.sendMessage(topic.getEditor().getText(), format.parse(payload.getText()));
            }          
        } catch (IllegalArgumentException ex) {
            MessageUtils.showException(MessageUtils.getRoot(this), resources.getString("label.sendmessage"), resources.getString("message.messageerror"), ex);
        }
        
        if (topic.getItems().contains(topic.getEditor().getText())) {
            topic.getSelectionModel().select(topic.getEditor().getText());
        } else {
           topic.getItems().add(0, topic.getEditor().getText());
           if (topic.getItems().size() > 20) {
               topic.getItems().remove(topic.getItems().size() - 1);
           }
           topic.getSelectionModel().select(0);
        }  
    }
}
