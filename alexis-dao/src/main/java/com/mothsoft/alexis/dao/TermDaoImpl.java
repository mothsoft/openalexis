///*   Copyright 2012 Tim Garrett, Mothsoft LLC
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package com.mothsoft.alexis.dao;
//
//import java.util.List;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.Query;
//
//import org.springframework.stereotype.Repository;
//
//import com.mothsoft.alexis.domain.PartOfSpeech;
//import com.mothsoft.alexis.domain.Term;
//
//@Repository
//public class TermDaoImpl implements TermDao {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public void setEm(final EntityManager em) {
//        this.em = em;
//    }
//
//    public void add(final Term term) {
//        this.em.persist(term);
//    }
//
//    public Term find(String termValue, PartOfSpeech partOfSpeech) {
//        final Query query = this.em
//                .createQuery("FROM Term WHERE value = :termValue AND partOfSpeechEnumValue = :partOfSpeech");
//        query.setParameter("termValue", termValue);
//        query.setParameter("partOfSpeech", (byte) partOfSpeech.getValue());
//        query.setHint("org.hibernate.cacheable", true);
//
//        @SuppressWarnings("unchecked")
//        final List<Term> results = query.getResultList();
//        final Term result = results != null && results.size() == 1 ? results.get(0) : null;
//        return result;
//    }
//
//}
