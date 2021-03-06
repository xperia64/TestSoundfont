package com.example.testsoundfont.models;

import android.net.Uri;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by Administrator on 2016-03-08.
 */
@Getter
@Setter
@AllArgsConstructor(suppressConstructorProperties = true)
@RequiredArgsConstructor
public class Song extends BaseModel implements Serializable {
    private String songName;
    private String filePath;
}
