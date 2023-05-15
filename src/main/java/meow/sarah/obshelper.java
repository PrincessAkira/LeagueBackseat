package meow.sarah;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.model.Scene;
import io.obswebsocket.community.client.model.SceneItem;
import org.json.JSONObject;

/**
 * @author https://github.com/PrincessAkira (Sarah)
 * Today is the 5/14/2023 @4:23 PM
 * This project is named LeagueBackseat
 * @description Worst fucking API to exist.
 */

public class obshelper {

    public static JSONObject jsonexample(boolean status, int id, String sceneName) {
        JSONObject jo = new JSONObject(
                "{{\n" +
                        "  \"op\": 8,\n" +
                        "  \"d\": {\n" +
                        "    \"requests\": [\n" +
                        "      {\n" +
                        "        \"requestType\": \"SetSceneItemEnabled\",\n" +
                        "        \"requestData\": {\n" +
                        "          \"sceneName\": \"test scene\",\n" +
                        "          \"sceneItemEnabled\": false\n" +
                        "        },\n" +
                        "        \"inputVariables\": {\n" +
                        "          \"sceneItemId\": \"sceneItemIdVariable\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}}"
        );
        // replace sceneItemIdVariable with the id of the scene item
        jo.getJSONObject("d").getJSONArray("requests").getJSONObject(0).getJSONObject("inputVariables").put("sceneItemId", id);
        // replace test scene with the name of the scene
        jo.getJSONObject("d").getJSONArray("requests").getJSONObject(0).getJSONObject("requestData").put("sceneName", sceneName);
        // replace sceneItemEnabled with true or false
        jo.getJSONObject("d").getJSONArray("requests").getJSONObject(0).getJSONObject("requestData").put("sceneItemEnabled", status);

        return jo;
    }

    public static int obsIndexID;

    static OBSRemoteController leagueController = new OBSRemoteControllerBuilder()
            .host("localhost")
            .lifecycle()
            .onReady(obshelper::init).and()
            .port(4455)
            .build();

    public static void init() {
        Logger.log("Connected to OBS!");
    }

    public static void checkScene() {
        // if there is no scene with the name "League", create one
        leagueController.getSceneList(getSceneListResponse -> {
            // check if there is a scene named LeagueInput
            if (getSceneListResponse.isSuccessful()) {
                // Print each Scene
                getSceneListResponse.getScenes().forEach(scene -> {
                    if (!scene.getSceneName().equals("LeagueInput")) {
                        leagueController.createScene("LeagueInput", createSceneResponse -> {
                            if (createSceneResponse.isSuccessful()) {
                                System.out.println("Scene created!");
                            }
                        });
                    }
                });
            }
        });
    }

    // yeah this is dirty, but it works so idc
    public static Integer getItemID(String scene) {
        leagueController.getSceneItemList(scene, getSceneItemListResponse -> {
            if (getSceneItemListResponse.isSuccessful()) {
                getSceneItemListResponse.getSceneItems().forEach(sceneItem -> {
                    if (sceneItem.getSourceName().equals("LeagueInput")) {
                        // why. cant. i return. this. value. without. it. being. null.
                        // and. why. is. this. not a. returnable. integer.??????????????????????????????????
                        obsIndexID = sceneItem.getSceneItemIndex();
                        //Logger.log(Integer.toString(obsIndexID));
                    }
                });
            }
        });
        return obsIndexID;
    }

    // for context. this MUST and should have worked. why doesnt it then? because the Websocket API Is cringe.
    public static void toggleSceneVisibility(boolean status) {
        leagueController.getSceneList(getSceneListResponse -> {
            if (getSceneListResponse.isSuccessful()) {
                getSceneListResponse.getScenes().forEach(scene1 -> {
                    if (scene1.getSceneName().equals(FileHelper.obsscene.trim())) {
                        try {
                            Logger.log("Toggling scene visibility...");
                            // timeout? no clue. better than callback
                            /*leagueController.setSceneItemEnabled(scene1.getSceneName(), getItemID(scene1.getSceneName()), status, setSceneItemEnabledResponse ->
                            {
                                if (setSceneItemEnabledResponse.isSuccessful()) {
                                    Logger.log("Scene visibility toggled!");
                                    Logger.log("Scene visibility: " + status);
                                }
                            }); */
                            Logger.log(jsonexample(status, getItemID(scene1.getSceneName()), scene1.getSceneName()).toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // we dont need this. Why? Dont ask me.

    public static SceneItem item(String scene) {
        SceneItem item = new SceneItem();
        leagueController.getSceneItemList(scene, getSceneItemListResponse -> {
            if (getSceneItemListResponse.isSuccessful()) {
                getSceneItemListResponse.getSceneItems().forEach(sceneItem -> {
                    if (sceneItem.toString().equals("LeagueInputText")) {
                        item.setSourceType("text_gdiplus");
                    }
                });
            }
        });
        return item;
    }

    // never used. Why? Dont ask me.
    public static Scene leagueInput() {
        Scene scene = new Scene("LeagueInput", leagueInput().getSceneIndex().intValue() + 1);
        leagueController.getSceneList(getSceneListResponse -> {
            if (getSceneListResponse.isSuccessful()) {
                getSceneListResponse.getScenes().forEach(scene1 -> {
                    if (scene1.getSceneName().equals("LeagueInput")) {
                        scene.setSceneName("LeagueInput");
                    }
                });
            }
        });
        return scene;
    }

    public void updateWebSource(String scene) {
        // update BrowserSource

        // i genunely found out why this isnt working.
        // IMPLEMENT THE DAMN 5.X CHANGES HOLY FUCK

    }


}
