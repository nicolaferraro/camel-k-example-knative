// camel-k: language=java trait=knative.sink-binding=false

import org.apache.camel.builder.RouteBuilder;

public class PredictionBridge extends RouteBuilder {

  @Override
  public void configure() throws Exception {

      from("knative:event/market.btc.usdt")
        .unmarshal().json()
        .transform().simple("${body[last]}")
        .log("Latest value for BTC/USDT is: ${body}")
        .marshal().json()
        .setHeader("Content-Type", constant("application/json"))
        .to("http://quarkus-ml.{{env:NAMESPACE}}.svc.cluster.local/samples?bridgeEndpoint=true")
        .to("direct:evaluate");

      
      from("direct:evaluate")
        .convertBodyTo(String.class)
        .choice()
          .when(body().isNotNull())
            .log("Emitting prediction...")
            .removeHeaders("*")
            .to("knative:event/prediction.quarkus");

  }

}
