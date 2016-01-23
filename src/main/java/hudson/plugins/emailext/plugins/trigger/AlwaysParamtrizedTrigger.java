package hudson.plugins.emailext.plugins.trigger;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;
import hudson.model.ParameterValue;
import hudson.model.TaskListener;
import hudson.plugins.emailext.plugins.EmailTrigger;
import hudson.plugins.emailext.plugins.EmailTriggerDescriptor;
import hudson.plugins.emailext.plugins.recipients.DevelopersRecipientProvider;
import hudson.plugins.emailext.plugins.recipients.FixedRecipientProvider;
import hudson.plugins.emailext.plugins.recipients.ListRecipientProvider;
import hudson.plugins.emailext.plugins.RecipientProvider;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author acearl
 */
public class AlwaysParamtrizedTrigger extends EmailTrigger {

    public static final String TRIGGER_NAME = "AlwaysParamtrized";
    
    @DataBoundConstructor
    public AlwaysParamtrizedTrigger(List<RecipientProvider> recipientProviders, String recipientList, String replyTo, String subject, String body, String attachmentsPattern, int attachBuildLog, String contentType) {
        super(recipientProviders, recipientList, replyTo, subject, body, attachmentsPattern, attachBuildLog, contentType);
    }
    
    @Deprecated
    public AlwaysParamtrizedTrigger(boolean sendToList, boolean sendToDevs, boolean sendToRequester, boolean sendToCulprits, String recipientList, String replyTo, String subject, String body, String attachmentsPattern, int attachBuildLog, String contentType) {
        super(sendToList, sendToDevs, sendToRequester, sendToCulprits,recipientList, replyTo, subject, body, attachmentsPattern, attachBuildLog, contentType);
    }

    @Override
    public boolean trigger(AbstractBuild<?, ?> build, TaskListener listener) {
		AbstractBuild<?, ?> previousFailedBuilds = build.getPreviousFailedBuild();
		boolean hasSameCurrentParameter = false;

		int buildNumber = 0;
		if (previousFailedBuilds != null) {
			buildNumber = previousFailedBuilds.getNumber();

			ParametersAction currentParameter = build.getAction(ParametersAction.class);
			ParametersAction previousFailedParameter = previousFailedBuilds.getAction(ParametersAction.class);
			hasSameCurrentParameter = hasSameCurrentParameter(currentParameter, previousFailedParameter);
			if (hasSameCurrentParameter) {
				listener.getLogger().println("Same parameter:" + currentParameter);
			}
		}

		listener.getLogger().println("Previous Failed build:" + buildNumber);

		return hasSameCurrentParameter;
	}

	private boolean hasSameCurrentParameter(ParametersAction currentParameter, ParametersAction previousFailedParameter) {
		boolean sameParameter = false;
		if (currentParameter != null) {
			sameParameter = true;
			List<ParameterValue> currentParameters = currentParameter.getParameters();
			for (ParameterValue currentP : currentParameters) {
				ParameterValue previousValue = previousFailedParameter.getParameter(currentP.getName());
				if (!previousValue.getValue().equals(currentP.getValue())) {
					sameParameter = false;
					break;
				}
			}
		}
		return sameParameter;
    }

    @Extension
    public static final class DescriptorImpl extends EmailTriggerDescriptor {
        
        public DescriptorImpl() {
            addDefaultRecipientProvider(new FixedRecipientProvider());
        }

        @Override
        public String getDisplayName() {
            return TRIGGER_NAME;
        }
        
        @Override
        public EmailTrigger createDefault() {
            return _createDefault();
        }
    }   
}

