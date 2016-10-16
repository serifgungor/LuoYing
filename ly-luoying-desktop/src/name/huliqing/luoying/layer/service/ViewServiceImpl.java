/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.layer.service;

import name.huliqing.luoying.data.ViewData;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.xml.DataFactory;
import name.huliqing.luoying.object.view.View;

/**
 *
 * @author huliqing
 */
public class ViewServiceImpl implements ViewService {

    @Override
    public void inject() {
        // ignore
    }

    @Override
    public View loadView(String viewId) {
        return loadView((ViewData) DataFactory.createData(viewId));
    }

    @Override
    public View loadView(ViewData data) {
        return Loader.loadView(data);
    }
    
}
