package meow.sarah;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.model.Scene;
import io.obswebsocket.community.client.model.SceneItem;

/**
 * @author https://github.com/PrincessAkira (Sarah)
 * Today is the 5/14/2023 @4:23 PM
 * This project is named LeagueBackseat
 * @description
 */

public class obshelper {

    public static void init() {
        Logger.log("Connecting to OBS...");
        checkScene();
    }    static OBSRemoteController leagueController = new OBSRemoteControllerBuilder()
            .host("localhost")
            .lifecycle()
            .onReady(obshelper::init).and()
            .port(4455)
            .build();

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

    public static void updateItemText(String scene, SceneItem item, String replacement) {

        // get the item LeagueInputText
        leagueController.getSceneItemList(scene, getSceneItemListResponse -> {
            if (getSceneItemListResponse.isSuccessful()) {
                getSceneItemListResponse.getSceneItems().forEach(sceneItem -> {
                    if (sceneItem.toString().equals("LeagueInputText")) {
                        item.setSourceName("LeagueInputText");
                        item.toString().replace(item.toString(), replacement);
                    }
                });
            }
        });

        // idk no way to update text yet?
      /*  leagueController.getInputPropertiesListPropertyItems(scene, item.toString(), getInputPropertiesListPropertyItemsResponse -> {
                // change text property to whatever
                if(getInputPropertiesListPropertyItemsResponse.isSuccessful()) {
                    getInputPropertiesListPropertyItemsResponse.getPropertyItems().forEach(propertyItem -> {
                        if(propertyItem.getItemName().equals("text")) {
                            propertyItem.getItemValue().replace(propertyItem.getItemValue(), replacement);
                        }
                    });
                }
        }); */
    }

    public void updateWebSource(String scene) {
        // update BrowserSource

    }


}
