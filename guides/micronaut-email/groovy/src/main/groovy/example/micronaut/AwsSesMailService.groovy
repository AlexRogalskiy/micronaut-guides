package example.micronaut

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Secondary
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.Body
import software.amazon.awssdk.services.ses.model.Content
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.Message
import software.amazon.awssdk.services.ses.model.SendEmailRequest
import software.amazon.awssdk.services.ses.model.SendEmailResponse

import jakarta.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

@CompileStatic
@Singleton // <1>
@Requires(condition = AwsResourceAccessCondition.class)  // <2>
@Secondary // <3>
public class AwsSesMailService implements EmailService {
    private static final Logger LOG = LoggerFactory.getLogger(AwsSesMailService.class)
    protected final String sourceEmail
    protected final SesClient ses

    public AwsSesMailService(@Nullable @Value('${AWS_REGION}') String awsRegionEnv, // <4>
                             @Nullable @Value('${AWS_SOURCE_EMAIL}') String sourceEmailEnv,
                             @Nullable @Value('${aws.region}') String awsRegionProp,
                             @Nullable @Value('${aws.sourceemail}') String sourceEmailProp) {

        this.sourceEmail = sourceEmailEnv != null ? sourceEmailEnv : sourceEmailProp
        String awsRegion = awsRegionEnv != null ? awsRegionEnv : awsRegionProp
        this.ses = SesClient.builder().region(Region.of(awsRegion)).build()
    }

    @Override
    void send(@NonNull @NotNull @Valid Email email) {
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(email.recipient).build())
                .source(sourceEmail)
                .message(Message.builder().subject(Content.builder().data(email.subject).build())
                        .body(Body.builder().text(Content.builder().data(email.textBody).build()).build()).build()).build() as SendEmailRequest
        SendEmailResponse response =ses.sendEmail(sendEmailRequest)
        LOG.info("Sent email with id: {}", response.messageId())
    }
}