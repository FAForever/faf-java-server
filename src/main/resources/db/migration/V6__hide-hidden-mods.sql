ALTER VIEW table_mod AS select
        v.id,
        v.uid,
        m.display_name as name,
        v.version,
        author,
        CASE v.type WHEN 'UI' THEN 1 WHEN 'SIM' THEN 0 END as ui,
        v.create_time as `date`,
        COALESCE(s.downloads, 0) as downloads,
        COALESCE(s.likes, 0) as likes,
        COALESCE(s.times_played, 0) as played,
        v.description,
        s.likers as likers,
        v.filename,
        v.icon,
        v.ranked
    from `mod` m
    join mod_version v on m.id = v.mod_id
    left join mod_stats s on m.id = s.mod_id
    where v.hidden = 0;