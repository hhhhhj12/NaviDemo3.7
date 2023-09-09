package cn.edu.cdut.navidemo3.ui.gallery;

import static cn.edu.cdut.navidemo3.ui.home.HomeFragment.FILE_FOLDER;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("文件保存路径："+FILE_FOLDER);
        mText.setValue("");
    }

    public LiveData<String> getText() {
        return mText;
    }
}