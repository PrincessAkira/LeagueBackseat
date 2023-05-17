package meow.sarah;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.message.response.sceneitems.GetSceneItemListResponse;
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse;
import io.obswebsocket.community.client.model.Scene;
import io.obswebsocket.community.client.model.SceneItem;

/**
 * @author https://github.com/PrincessAkira (Sarah)
 * Today is the 5/14/2023 @4:23 PM
 * This project is named LeagueBackseat
 * @description Worst fucking API to exist.
 */

public class obshelper {

    public static boolean isRunning = false;

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
        GetSceneItemListResponse getSceneItemListResponse = leagueController.getSceneItemList(scene, 10_000);
        if (getSceneItemListResponse.isSuccessful()) {
            return getSceneItemListResponse.getSceneItems()
                    .stream()
                    .filter(sceneItem -> sceneItem.getSourceName().equals("LeagueInput"))
                    .findFirst()
                    .map(SceneItem::getSceneItemId)
                    .orElse(null);
        }
        return null;
    }

    public static void toggleSceneVisibility(boolean status) {
        GetSceneListResponse getSceneListResponse = leagueController.getSceneList(10_000);
        if (getSceneListResponse.isSuccessful()) {
            getSceneListResponse.getScenes().forEach(scene1 -> {
                if (scene1.getSceneName().equals(FileHelper.obsscene.trim())) {
                    try {
                        leagueController.setSceneItemEnabled(scene1.getSceneName(), getItemID(scene1.getSceneName()), status, 100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
}
