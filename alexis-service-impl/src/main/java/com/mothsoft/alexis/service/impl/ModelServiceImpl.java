/*   Copyright 2012 Tim Garrett, Mothsoft LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mothsoft.alexis.service.impl;

import java.io.File;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.ModelDao;
import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Model;
import com.mothsoft.alexis.service.ModelService;

@Service
@Transactional
public class ModelServiceImpl implements ModelService {

    private ModelDao dao;

    private static final String BIN_GZ_EXT = ".bin.gz";

    private File baseDirectory;

    public ModelServiceImpl(ModelDao dao, File baseDirectory) {
        this.dao = dao;
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void add(Model model) {
        dao.add(model);
    }

    @Override
    public Model findModelByUserAndName(Long userId, String name) {
        return dao.findByUserAndName(userId, name);
    }

    @Override
    public Model get(Long id) {
        return dao.get(id);
    }

    @Override
    public DataRange<Model> listByOwner(Long userId, int first, int count) {
        return dao.listByOwner(userId, first, count);
    }

    @Override
    public void remove(Model model) {
        dao.remove(model);
        final File directory = new File(this.baseDirectory, "" + model.getUserId());
        // FIXME - this is probably highly specific to one model type...
        final File file = new File(directory, "" + model.getId() + BIN_GZ_EXT);
        file.delete();
    }

}
