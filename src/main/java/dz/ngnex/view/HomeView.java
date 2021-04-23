/*
 * Handcrafted with love by Youcef DEBBAH
 * Copyright 2019 youcef-debbah@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dz.ngnex.view;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;

@Named
public class HomeView {

    private List<Image> images = Arrays.asList(new Image[] {
            new Image("Image 1/5", "image 1/5", "image-1.jpg"),
            new Image("Image 2/5", "image 2/5", "image-2.jpg"),
            new Image("Image 3/5", "image 3/5", "image-3.jpg"),
            new Image("Image 4/5", "image 4/5", "image-4.jpg"),
            new Image("Image 5/5", "image 5/5", "image-5.jpg"),
    });

    public List<Image> getImages() {
        return images;
    }
}
