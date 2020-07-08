// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class PredictionBridge extends RouteBuilder {

  @Override
  public void configure() throws Exception {

      from("knative:event/market.btc.usdt")
        .unmarshal().json()
        .transform().simple("${body[last]}")
        .log("Latest value for BTC/USDT is: ${body}")
        .marshal().json()
        .removeHeaders("*")
        .setHeader("Content-Type", constant("application/json"))
        .to("netty-http:http://quarkus-ml.{{env:NAMESPACE}}.svc.cluster.local/samples")
        .removeHeaders("*")
        .wireTap("direct:evaluate")
        .setBody().constant("");

      
      from("direct:evaluate")
        .convertBodyTo(String.class)
        .choice()
          .when(body().isNotEqualTo(""))
            .log("Emitting prediction...")
            .to("knative:event/prediction.quarkus");

  }

}
