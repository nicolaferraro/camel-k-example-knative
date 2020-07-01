// camel-k: language=java property-file=telegram.properties

import org.apache.camel.builder.RouteBuilder;

public class TelegramSink extends RouteBuilder {
  @Override
  public void configure() throws Exception {

    from("knative:event/predictor.better")
      .unmarshal().json()
      .transform().simple("Predictor suggests to ${body[operation]} at price ${body[value]}")
      .log("${body}")
      .to("telegram:bots?chatId={{telegram.chat}}");

  }
}
