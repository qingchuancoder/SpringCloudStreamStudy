package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author qingchuan
 * @date 2024-08-04
 */
@EnableTestBinder
class SampleTests {

    @Test
    public void testProcess() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(
                        Application.class))
                .run("--spring.cloud.function.definition=uppercase")) {
            InputDestination source = context.getBean(InputDestination.class);
            OutputDestination target = context.getBean(OutputDestination.class);
            source.send(new GenericMessage<>("hello".getBytes()));
            assertThat(target.receive().getPayload()).isEqualTo("HELLO".getBytes());
        }
    }

    @Test
    void testConsumer() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(
                        Application.class))
                .run("--spring.cloud.function.definition=log")) {
            InputDestination source = context.getBean(InputDestination.class);
            source.send(new GenericMessage<>("hello".getBytes()));
        }
    }


    @Test
    void testMultipleFunctions() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                TestChannelBinderConfiguration.getCompleteConfiguration(Application.class)
        ).run("--spring.cloud.function.definition=uppercase;reverse")) {

            InputDestination inputDestination = context.getBean(InputDestination.class);
            OutputDestination outputDestination = context.getBean(OutputDestination.class);

            Message<byte[]> inputMessage = MessageBuilder.withPayload("Hello".getBytes()).build();
            inputDestination.send(inputMessage, "uppercase-in-0");
            inputDestination.send(inputMessage, "reverse-in-0");

            Message<byte[]> outputMessage = outputDestination.receive(0, "uppercase-out-0");
            assertThat(outputMessage.getPayload()).isEqualTo("HELLO".getBytes());

            outputMessage = outputDestination.receive(0, "reverse-out-0");
            assertThat(outputMessage.getPayload()).isEqualTo("olleH".getBytes());
        }
    }
}
