package me.himi.love.entity.loader;

import me.himi.love.IAppService.UploadFaceResponse;

/**
 * 
 * @ClassName:IUploadFileResponseLoader
 * @author sparklee liduanwei_911@163.com
 * @date Nov 4, 2014 9:00:59 PM
 */
public interface IUploadFileResponseLoader {
    UploadFaceResponse load(String response);
}
