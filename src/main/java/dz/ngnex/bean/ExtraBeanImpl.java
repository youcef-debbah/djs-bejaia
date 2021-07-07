package dz.ngnex.bean;

import dz.ngnex.entity.ExtraEntity;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Stateless
@TestWithTransaction
public class ExtraBeanImpl implements ExtraBean {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    private ExtraEntity getExtra(Integer id) {
        return id != null ? em.find(ExtraEntity.class, id) : null;
    }

    @Override
    public String get(Integer id) {
        return ExtraEntity.getValue(getExtra(id));
    }

    @Override
    public Map<Integer, String> getAll(Integer category) {
        List<ExtraEntity> entities = em.createQuery("select e from ExtraEntity e where e.category = :category", ExtraEntity.class)
                .setParameter("category", category)
                .getResultList();

        LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
        for (ExtraEntity entity : entities)
            result.put(entity.getId(), entity.getValue());
        return result;
    }

    @Override
    public String put(int id, String value) {
        return put(id, value, null);
    }

    @Override
    public String put(int id, String value, Integer category) {
        String oldValue = remove(id);

        if (value != null)
            em.persist(new ExtraEntity(id, value, category));

        return oldValue;
    }

    @Override
    public String remove(Integer id) {
        ExtraEntity entity = getExtra(id);
        if (entity != null)
            em.remove(entity);

        return ExtraEntity.getValue(entity);
    }

    @Override
    public void removeAll(Integer category) {
        em.createQuery("delete from ExtraEntity e where e.category = :category")
                .setParameter("category", category)
                .executeUpdate();
    }

    @Override
    @TestWithTransaction(traceSQL = false)
    public void clear() {
        BeanUtil.clearCache(em);
    }

    @AroundInvoke
    public Object benchmarkCalls(InvocationContext ctx) throws Exception {
        return BeanUtil.benchmarkCall(log, ctx);
    }
}
