package seemoo.fitbit.tasks;

import android.util.Log;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.https.HttpsClient;
import seemoo.fitbit.interactions.Interactions;
import seemoo.fitbit.miscellaneous.ConstantValues;

/**
 * Creates a task for an interaction.
 */
class InteractionsTask extends Task {

    private Interactions interactions;
    private String type;
    private HttpsClient client;
    private Tasks tasks;
    private WorkActivity activity;

    private String interactionName = null; //name of the corresponding interaction WITHOUT ...Interaction

    /**
     * Creates an instance of interaction task for all supported instructions, except upload.
     *
     * @param interactions The instance of interaction.
     * @param type         The type of the interaction.
     * @param tasks        The instance of tasks.
     * @param activity     The current activity.
     */
    InteractionsTask(Interactions interactions, String type, Tasks tasks, WorkActivity activity) {
        this.interactions = interactions;
        this.type = type;
        this.tasks = tasks;
        this.activity = activity;
    }

    /**
     * Creates an instance of interaction task for upload instructions.
     *
     * @param interactions The instance of interaction.
     * @param type         The type of the interaction.
     * @param tasks        The instance of tasks.
     * @param client       The instance of https client.
     */
    InteractionsTask(Interactions interactions, String type, Tasks tasks, HttpsClient client) { //for https
        this.interactions = interactions;
        this.type = type;
        this.tasks = tasks;
        this.client = client;
    }

    /**
     * {@inheritDoc}
     * Executes the to the type corresponding instruction. Supported instructions are:
     * - authentication
     * - get a microdump
     * - get a megadump
     * - upload a microdump
     * - upload a megadump
     */
    @Override
    public void execute() {
        switch (type) {
            case "authentication":
                interactions.intAuthentication();
                interactionName = "Authentication";
                setTimer(-1);
                break;
            case ConstantValues.INFORMATION_MICRODUMP:
                interactions.intMicrodump();
                interactionName = "Dump";
                break;
            case ConstantValues.INFORMATION_MEGADUMP:
                interactions.intMegadump();
                interactionName = "Dump";
                setTimer(60000);
                break;
            case ConstantValues.INFORMATION_MICRODUMP + "Upload":
                if (!client.getResponse().equals("")) {
                    interactions.intUploadMicroDumpInteraction(client.getResponse());
                    interactionName = "Upload";
                } else {
                    Log.e(TAG, "Error: Nothing to Upload");
                    tasks.taskFinished();
                }
                break;
            case ConstantValues.INFORMATION_MEGADUMP + "Upload":
                String response = client.getResponse();
                if (response != null && !("").equals(response)) {
                    interactions.intUploadMegadumpInteraction(response);
                    interactionName = "Upload";
                } else {
                    Log.e(TAG, "Error: Nothing to Upload");
                    tasks.taskFinished();
                }
                break;
            default:
                Log.e(TAG, "Error: Wrong interaction task type: " + type);
                tasks.taskFinished();
        }
    }

    /**
     * Returns the interaction name to execute.
     *
     * @return The name of the interaction.
     */
    String getInteractionName() {
        return interactionName;
    }
}
