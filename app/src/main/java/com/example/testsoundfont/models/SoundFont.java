package com.example.testsoundfont.models;

import android.net.Uri;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by Administrator on 2016-03-09.
 */
@Getter
@Setter
@AllArgsConstructor(suppressConstructorProperties = true)
@RequiredArgsConstructor
public class SoundFont extends BaseModel implements Serializable {
    private String sfName;
    private String filePath;
    private boolean using = false;
}
